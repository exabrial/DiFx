package com.github.exabrial.difx.fxmlscoped.context;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.difx.fxmlscoped.FxmlScoped;

public class FxmlScopeContext implements Context {
	private static final Logger log = LoggerFactory.getLogger(FxmlScopeContext.class);

	private final ThreadLocal<ScopeNode> activeNode = new ThreadLocal<>();
	private final ConcurrentMap<String, ScopeNode> nodesByKey = new ConcurrentHashMap<>();

	@Override
	public Class<? extends Annotation> getScope() {
		return FxmlScoped.class;
	}

	@Override
	public boolean isActive() {
		return activeNode.get() != null;
	}

	@Override
	public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
		final ScopeNode startNode = activeNode.get();
		final T result;
		if (startNode == null) {
			throw new ContextNotActiveException("@FxmlScoped context is not active; no FXML view is loading");
		} else {
			final T existing = findUpward(startNode, contextual);
			if (existing == null) {
				result = createInNode(startNode, contextual, creationalContext);
			} else {
				result = existing;
			}
		}
		return result;
	}

	@Override
	public <T> T get(final Contextual<T> contextual) {
		final ScopeNode startNode = activeNode.get();
		final T result;
		if (startNode == null) {
			throw new ContextNotActiveException("@FxmlScoped context is not active; no FXML view is loading");
		} else {
			result = findUpward(startNode, contextual);
		}
		return result;
	}

	/**
	 * Opens a child node under the currently-active node, keyed by the given view key, and makes it the active node. Returns the
	 * previously-active node's key so the caller can restore it via {@link #exit(String)} (null when the new node is a root).
	 */
	public String enter(final String viewKey) {
		final ScopeNode parent = activeNode.get();
		final String previousKey;
		if (parent != null && parent.closed) {
			throw new IllegalStateException("cannot open @FxmlScoped child under closed node parentKey:" + parent.key);
		} else {
			final ScopeNode node = new ScopeNode(viewKey, parent);
			if (parent == null) {
				previousKey = null;
			} else {
				parent.children.add(node);
				previousKey = parent.key;
			}
			nodesByKey.put(viewKey, node);
			activeNode.set(node);
			log.debug("enter() activated @FxmlScoped node viewKey:{} parentKey:{}", viewKey, previousKey);
		}
		return previousKey;
	}

	/**
	 * Restores the active node to the one identified by the given key, or clears the active node when the key is null.
	 */
	public void exit(final String previousKey) {
		final ScopeNode previousNode;
		if (previousKey == null) {
			previousNode = null;
		} else {
			previousNode = nodesByKey.get(previousKey);
		}
		if (previousNode == null) {
			activeNode.remove();
		} else {
			activeNode.set(previousNode);
		}
		log.debug("exit() restored active @FxmlScoped node previousKey:{}", previousKey);
	}

	/**
	 * Destroys the node for the given key and all of its descendants, leaf-first, running {@code @PreDestroy} on each instance.
	 */
	public void destroy(final String viewKey) {
		final ScopeNode node = nodesByKey.get(viewKey);
		if (node == null) {
			log.debug("destroy() no @FxmlScoped node for viewKey:{}", viewKey);
		} else {
			destroyNode(node);
		}
	}

	/**
	 * Returns a snapshot of every live instance of the given contextual across all open nodes. Used to multicast a CDI event to all
	 * matching live {@code @FxmlScoped} instances.
	 */
	public List<Object> liveInstances(final Contextual<?> contextual) {
		final List<Object> snapshot = new ArrayList<>();
		for (final ScopeNode node : nodesByKey.values()) {
			if (!node.closed) {
				final InstanceHolder<?> holder = node.instances.get(contextual);
				if (holder != null) {
					snapshot.add(holder.instance);
				}
			}
		}
		return snapshot;
	}

	/**
	 * Returns the key of the currently-active node, or null when no node is active. Used to bind an {@code FxmlNode} handle to the node
	 * active at an injecting bean's construction.
	 */
	public String currentNodeKey() {
		final ScopeNode node = activeNode.get();
		final String key;
		if (node == null) {
			key = null;
		} else {
			key = node.key;
		}
		return key;
	}

	/**
	 * Re-activates the existing node for the given key for the duration of the action, restoring the prior active node afterward. A
	 * child loaded inside the action parents to this node. Used for dynamic child creation outside the original load and for activating
	 * an instance's own node around an inbound event invocation.
	 */
	public void runInNode(final String nodeKey, final Runnable action) {
		final ScopeNode node = nodesByKey.get(nodeKey);
		if (node == null) {
			throw new IllegalStateException("no @FxmlScoped node for key:" + nodeKey);
		} else {
			final ScopeNode previous = activeNode.get();
			activeNode.set(node);
			try {
				action.run();
			} finally {
				if (previous == null) {
					activeNode.remove();
				} else {
					activeNode.set(previous);
				}
			}
		}
	}

	/**
	 * Invokes the action with each live instance of the given contextual, with that instance's node activated for the duration of the
	 * call so an inbound event observer's body can load children that parent correctly. Iterates a snapshot of the open nodes; a node
	 * destroyed concurrently is skipped.
	 */
	public void forEachLiveInstance(final Contextual<?> contextual, final Consumer<Object> action) {
		for (final ScopeNode node : nodesByKey.values()) {
			if (!node.closed) {
				final InstanceHolder<?> holder = node.instances.get(contextual);
				if (holder != null) {
					final Object instance = holder.instance;
					runInNode(node.key, () -> action.accept(instance));
				}
			}
		}
	}

	private void destroyNode(final ScopeNode node) {
		node.closed = true;
		for (final ScopeNode child : node.children) {
			destroyNode(child);
		}
		for (final Map.Entry<Contextual<?>, InstanceHolder<?>> entry : node.instances.entrySet()) {
			destroyHolder(entry.getKey(), entry.getValue());
		}
		node.instances.clear();
		nodesByKey.remove(node.key);
		if (node.parent != null) {
			node.parent.children.remove(node);
		}
		log.debug("destroyNode() destroyed @FxmlScoped node viewKey:{}", node.key);
	}

	private static <T> T findUpward(final ScopeNode startNode, final Contextual<T> contextual) {
		T found = null;
		ScopeNode cursor = startNode;
		while (cursor != null && found == null) {
			final InstanceHolder<?> holder = cursor.instances.get(contextual);
			if (holder != null) {
				found = castInstance(holder);
			}
			cursor = cursor.parent;
		}
		return found;
	}

	private static <T> T createInNode(final ScopeNode node, final Contextual<T> contextual,
			final CreationalContext<T> creationalContext) {
		final T instance;
		if (node.closed) {
			throw new IllegalStateException("cannot create @FxmlScoped instance in closed node viewKey:" + node.key);
		} else {
			instance = contextual.create(creationalContext);
			node.instances.put(contextual, new InstanceHolder<>(instance, creationalContext));
			log.debug("createInNode() created @FxmlScoped instance in node viewKey:{} contextual:{}", node.key, contextual);
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	private static <T> T castInstance(final InstanceHolder<?> holder) {
		return (T) holder.instance;
	}

	@SuppressWarnings("unchecked")
	private static void destroyHolder(final Contextual<?> contextual, final InstanceHolder<?> holder) {
		try {
			((Contextual<Object>) contextual).destroy(holder.instance, (CreationalContext<Object>) holder.creationalContext);
		} catch (final RuntimeException destroyException) {
			log.error("destroyHolder() error destroying @FxmlScoped instance contextual:{}", contextual, destroyException);
		}
	}

	private static final class ScopeNode {
		private final String key;
		private final ScopeNode parent;
		private final Set<ScopeNode> children = ConcurrentHashMap.newKeySet();
		private final ConcurrentMap<Contextual<?>, InstanceHolder<?>> instances = new ConcurrentHashMap<>();
		private volatile boolean closed;

		private ScopeNode(final String key, final ScopeNode parent) {
			this.key = key;
			this.parent = parent;
		}
	}

	private static final class InstanceHolder<T> {
		private final T instance;
		private final CreationalContext<T> creationalContext;

		private InstanceHolder(final T instance, final CreationalContext<T> creationalContext) {
			this.instance = instance;
			this.creationalContext = creationalContext;
		}
	}
}