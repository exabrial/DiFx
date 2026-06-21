# DiFx

CDI dependency injection for JavaFX. The missing piece to make Desktop Development Awesome!

DiFx wires a Jakarta CDI SE container into JavaFX's lifecycle so your controllers, presenters, and services are proper managed beans. `@Inject`, custom scopes, Sync/Async events, interceptors, all of should work. It's a portable CDI extension, so it works with OpenWebBeans and (in theory, haven't tested) Weld, or any other spec-compliant container.

## The Project: DiFx

**D**ependency **I**njection Java**Fx**

Honestly I couldn't think of a better name. Adam Bien already claimed AfterBurrner.Fx (what a sick project name).

### Why

JavaFX's `FXMLLoader` normall instantiates controllers by plain-ole reflection. Oof. You get a bare object and that kinda stinks to wire everything by hand. The usual workarounds (service locators, static holders, framework-specific hacks) create a bunch of work. It'd sure be nice to have all of the convieniences of CDI!

DiFx plugs into `FXMLLoader`'s controller factory so that every `fx:controller` class, including nested `fx:include` controllers, is resolved from the CDI container. Your controllers are real beans. You `@Inject` your dependencies, observe CDI events, and let the container handle lifecycle management.

### Speeed

A full app with OpenWebBeans boots in about 100ms with moderately complicated desktop applications on my M4. I don't think you'll notice!

### What's in the box

![DiFx component overview](doc/components.svg)

**Bootstrap.** `DiFxLauncher` starts the CDI container, then hands off to JavaFX. The container stays open for the full application lifecycle and closes on exit. `DiFxApplication` is the `Application` base class that self-injects against the running container in `init()`, so your `@Inject` fields resolve even though JavaFX created the instance by reflection.

**Startup event.** When JavaFX calls `start(Stage)`, DiFx fires the primary `Stage` as a CDI event qualified with `@StartupStage`. Any managed bean can observe it to build the initial window. Just write `@Observes @StartupStage Stage primaryStage` on a method and you're in.

**CDI-aware FXML loading.** `DiFxViewLoader` loads an FXML file and resolves its controller (and any nested controllers) from the container. It returns a `FxControllerAndView<C>` bundling the controller, the root `Parent` node, and the `CreationalContext` so you can destroy dependent beans cleanly when the view is torn down.

**`@FxmlView` annotation.** Maps a controller class to its FXML resource. By default the loader looks for `ControllerName.fxml` in the same package. Annotate with `@FxmlView("custom.fxml")` to override.

**`UiExecutor`.** A `java.util.concurrent.Executor` that runs work on the FX Application Thread (inline if you're already on it). Inject it into presenters and use it with `CompletionStage.thenAcceptAsync(action, uiExecutor)` to marshal results back to the UI thread. Beats scattering `Platform.runLater` calls around your codebase.

### Getting started

#### Maven

```xml
<dependency>
    <groupId>com.github.exabrial.javafx</groupId>
    <artifactId>difx</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 1. Write a launcher

The launcher boots CDI, then starts JavaFX. Override `configureContainer` if you need to customize the `SeContainerInitializer` (add packages, enable interceptors, etc.).

```java
public class MyLauncher extends DiFxLauncher {
    @Override
    protected Class<? extends Application> applicationClass() {
        return MyApplication.class;
    }

    public static void main(String[] args) {
        new MyLauncher().launch(args);
    }
}
```

#### 2. Write the application class

Extend `DiFxApplication`. You can `@Inject` fields here; they'll be resolved after the container starts but before `start()` is called.

```java
public class MyApplication extends DiFxApplication {
    @Inject
    private SomeService service;

    @Override
    protected void afterInjection() throws Exception {
        // runs after injection, before start()
    }
}
```

#### 3. Observe the startup stage

Instead of overriding `start()` and packing all your setup in there, observe the `@StartupStage` event from any managed bean:

```java
@ApplicationScoped
public class ShellPresenter {
    @Inject
    private DiFxViewLoader viewLoader;

    public void onStartup(@Observes @StartupStage Stage primaryStage) {
        FxControllerAndView<MainController> main = viewLoader.load(MainController.class);
        primaryStage.setScene(new Scene(main.view(), 800, 600));
        primaryStage.setTitle("My App");
        primaryStage.show();
    }
}
```

#### 4. Write controllers as CDI beans

Annotate your FXML controllers with a scope (typically `@Dependent`). The controller factory resolves them from the container, so `@Inject` works alongside `@FXML`:

```java
@Dependent
public class MainController {
    @Inject
    private Event<SomeDomainEvent> domainEventBus;

    @Inject
    private SomeViewModel viewModel;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.textProperty().bind(viewModel.statusProperty());
    }

    @FXML
    public void onButtonClick() {
        domainEventBus.fire(new SomeDomainEvent("clicked"));
        domainEventBus.fireAsync(new SomeDomainEvent("clicked"));
    }
}
```

#### 5. Use CDI events for decoupled communication

Fire domain events from controllers, observe them in presenters, write to shared view-models. Synchronous observers (`@Observes`) run on the firing thread (the FX thread). Async observers (`@ObservesAsync`) run on a container-managed thread, so use `UiExecutor` to marshal view-model writes back:

```java
@ApplicationScoped
public class SomePresenter {

    @Inject
    private SomeViewModel viewModel;

    @Inject
    private UiExecutor uiExecutor;

    public void onEvent(@Observes SomeDomainEvent event) {
        // on the FX thread already, write the view-model directly
        viewModel.setStatus("handled synchronously");
    }

    public void onEventAsync(@ObservesAsync SomeDomainEvent event) {
        // off the FX thread, do expensive work here
        String result = doExpensiveWork();
        uiExecutor.execute(() -> viewModel.setStatus(result));
    }
}
```

#### 6. Clean up transient views

One weird quirk is that if you use `@FXML` "injections", the JavaFx framework sets the fields via property reflection. Since CDI Proxies for something like an `@ApplicationScoped` bean don't pass field mutations through to proxy targets, you have two choices:

1. Annotate setters with `@FXML` which does work and will proxy through.
2. Keep field injections, but using `@DependentScoped` beans. This works because `@DependentScoped` are proxies; its the real thing. Honestly this is a pretty natural feeling lifecycle for Desktop Applications anyway.

So in light of #2: `FxControllerAndView` is `AutoCloseable`. Closing it releases the `CreationalContext`, destroying dependent beans and firing `@PreDestroy`. Use this for dialogs and other short-lived views:

```java
try (FxControllerAndView<DialogController> dialog = viewLoader.load(DialogController.class)) {
    // show the dialog, wait for it to close
}
// controller and its dependencies are now destroyed
```

For the main shell view that lives the whole run, just don't close it.

## Architecture

DiFx doesn't force an architecture on you, but the pieces fit naturally into a **Controller / Presenter / ViewModel** separation:

![Architecture diagram](doc/architecture.svg)

**Controller** (`@Dependent`, FXML-bound). Thin. Binds widgets to view-model properties and fires domain events on user actions. No business logic here.

**Presenter** (`@ApplicationScoped`). Observes domain events, does the actual work (or delegates to services), and writes view-model state. Uses `UiExecutor` when arriving from a background thread.

**ViewModel** (`@ApplicationScoped`). Holds JavaFX properties. This is the shared contract between controller and presenter; neither side references the other directly.

Leverage CDI's event bus to distribute events around your application!

### Under the hood

DiFx registers itself via `META-INF/services/jakarta.enterprise.inject.spi.Extension`. The `DiFxExtension` adds its beans during `BeforeBeanDiscovery`, so they're available regardless of the application's discovery mode. DiFx's own jar uses `bean-discovery-mode="none"` so these types are registered exactly once through the extension, not picked up again by classpath scanning.

`FxmlLoaderProducer` is a `@Dependent` CDI producer that creates single-use `FXMLLoader` instances with a controller factory backed by `BeanManager.getReference()`. All controllers resolved during a single FXML load share one `CreationalContext`, so they can be destroyed together when the view is torn down.

### Requirements

- Java 25+
- Jakarta CDI 4.0+
- JavaFX 25+
- A CDI SE container implementation (OpenWebBeans, Weld SE, etc.)

## License and other boring legal notes

* All files in this project are copyrighted
* This license allows you to safely use unmodified/un-extended code in closed-source commercial projects, without revealing your company's proprietary application code in most cases.
    - However: Note that if you modify/extend DiFx, distribute it, and/or offer online access to apps through a modified/extended Petrify, it is required by law that the source code for your DiFx changeset be made available _first_, before offering said access to your app or distribution.
    - Again, this does not include your proprietary application source code, just the changeset to DiFx.
* Java, JavaFx, Apache, Weld, OpenWebBeans, and many other names are trademarks; this project is not endorsed by nor affiliated with them.
    
