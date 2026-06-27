package com.github.exabrial.difx.fxmlscoped;

import com.github.exabrial.difx.fxmlscoped.context.FxmlScopeContext;

public class FxmlNode {
	private final FxmlScopeContext context;
	private final String nodeKey;

	FxmlNode(final FxmlScopeContext context, final String nodeKey) {
		this.context = context;
		this.nodeKey = nodeKey;
	}

	public void run(final Runnable action) {
		context.runInNode(nodeKey, action);
	}

	public String key() {
		return nodeKey;
	}
}
