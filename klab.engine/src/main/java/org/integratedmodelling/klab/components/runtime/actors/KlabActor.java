package org.integratedmodelling.klab.components.runtime.actors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.ehcache.impl.internal.events.ScopedStoreEventDispatcher;
import org.integratedmodelling.kactors.api.IKActorsBehavior;
import org.integratedmodelling.kactors.api.IKActorsBehavior.Type;
import org.integratedmodelling.kactors.api.IKActorsStatement;
import org.integratedmodelling.kactors.api.IKActorsStatement.Assert;
import org.integratedmodelling.kactors.api.IKActorsStatement.Assert.Assertion;
import org.integratedmodelling.kactors.api.IKActorsStatement.Assignment;
import org.integratedmodelling.kactors.api.IKActorsStatement.Call;
import org.integratedmodelling.kactors.api.IKActorsStatement.ConcurrentGroup;
import org.integratedmodelling.kactors.api.IKActorsStatement.Do;
import org.integratedmodelling.kactors.api.IKActorsStatement.FireValue;
import org.integratedmodelling.kactors.api.IKActorsStatement.For;
import org.integratedmodelling.kactors.api.IKActorsStatement.If;
import org.integratedmodelling.kactors.api.IKActorsStatement.Instantiation;
import org.integratedmodelling.kactors.api.IKActorsStatement.Sequence;
import org.integratedmodelling.kactors.api.IKActorsStatement.TextBlock;
import org.integratedmodelling.kactors.api.IKActorsStatement.While;
import org.integratedmodelling.kactors.api.IKActorsValue;
import org.integratedmodelling.kactors.api.IKActorsValue.ExpressionType;
import org.integratedmodelling.kactors.model.KActorsActionCall;
import org.integratedmodelling.kactors.model.KActorsArguments;
import org.integratedmodelling.kactors.model.KActorsValue;
import org.integratedmodelling.kim.api.IKimExpression;
import org.integratedmodelling.kim.api.IKimObservable;
import org.integratedmodelling.klab.Actors;
import org.integratedmodelling.klab.Actors.CallDescriptor;
import org.integratedmodelling.klab.Actors.Library;
import org.integratedmodelling.klab.Annotations;
import org.integratedmodelling.klab.Configuration;
import org.integratedmodelling.klab.Extensions;
import org.integratedmodelling.klab.Logging;
import org.integratedmodelling.klab.Observables;
import org.integratedmodelling.klab.Urn;
import org.integratedmodelling.klab.api.actors.IBehavior;
import org.integratedmodelling.klab.api.actors.IBehavior.Action;
import org.integratedmodelling.klab.api.auth.IActorIdentity;
import org.integratedmodelling.klab.api.auth.IActorIdentity.KlabMessage;
import org.integratedmodelling.klab.api.auth.IActorIdentity.KlabMessage.Semaphore;
import org.integratedmodelling.klab.api.auth.IIdentity;
import org.integratedmodelling.klab.api.auth.IRuntimeIdentity;
import org.integratedmodelling.klab.api.data.general.IExpression.CompilerOption;
import org.integratedmodelling.klab.api.knowledge.IObservable;
import org.integratedmodelling.klab.api.model.IAnnotation;
import org.integratedmodelling.klab.api.monitoring.IMessage;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.auth.EngineUser;
import org.integratedmodelling.klab.components.runtime.actors.KlabActionExecutor.Actor;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.AddComponentToGroup;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.AppReset;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.BindUserAction;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.Cleanup;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.ComponentFire;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.Fire;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.KActorsMessage;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.Load;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.Spawn;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.Stop;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.UserAction;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.UserMenuAction;
import org.integratedmodelling.klab.components.runtime.actors.UserBehavior.UnknownMessage;
import org.integratedmodelling.klab.components.runtime.actors.ViewBehavior.GroupHandler;
import org.integratedmodelling.klab.components.runtime.actors.ViewBehavior.KlabWidgetActionExecutor;
import org.integratedmodelling.klab.components.runtime.actors.behavior.Behavior.Match;
import org.integratedmodelling.klab.components.runtime.observations.Observation;
import org.integratedmodelling.klab.engine.runtime.Session;
import org.integratedmodelling.klab.engine.runtime.ViewImpl;
import org.integratedmodelling.klab.engine.runtime.api.IRuntimeScope;
import org.integratedmodelling.klab.engine.runtime.code.ObjectExpression;
import org.integratedmodelling.klab.exceptions.KlabActorException;
import org.integratedmodelling.klab.exceptions.KlabException;
import org.integratedmodelling.klab.exceptions.KlabUnimplementedException;
import org.integratedmodelling.klab.rest.Layout;
import org.integratedmodelling.klab.rest.ViewAction;
import org.integratedmodelling.klab.rest.ViewComponent;
import org.integratedmodelling.klab.utils.MapUtils;
import org.integratedmodelling.klab.utils.Pair;
import org.integratedmodelling.klab.utils.Parameters;
import org.integratedmodelling.klab.utils.Path;
import org.integratedmodelling.klab.utils.Triple;
import org.integratedmodelling.klab.utils.Utils;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ReceiveBuilder;

/**
 * The base actor implementation for k.Actors. Linked to an identity and capable of interpreting
 * k.Actors code, whose interpreter is implemented here.
 * 
 * @author Ferd
 */
public class KlabActor extends AbstractBehavior<KlabMessage> {

    public static class ActorReference implements IActorIdentity.Reference {
        public ActorReference(ActorRef<KlabMessage> actor) {
            this.actor = actor;
        }

        public ActorRef<KlabMessage> actor;

        @Override
        public void tell(KlabMessage message) {
            actor.tell(message);
        }
    }

    protected IBehavior behavior;
    /*
     * this is set when a behavior is loaded and used to create proper actor paths for application
     * components, so that user messages can be sent to the main application actor and directed to
     * the actor that implements them.
     */
    private String childActorPath = null;
    protected String appId;
    protected IActorIdentity<KlabMessage> identity;
    protected Map<Long, MatchActions> listeners = Collections.synchronizedMap(new HashMap<>());
    protected Map<String, MatchActions> componentFireListeners = Collections.synchronizedMap(new HashMap<>());
    private AtomicLong nextId = new AtomicLong(0);
    private Map<String, Long> actionBindings = Collections.synchronizedMap(new HashMap<>());
    private Map<String, ActorRef<KlabMessage>> receivers = Collections.synchronizedMap(new HashMap<>());
    private Map<String, List<ActorRef<KlabMessage>>> childInstances = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Object> symbolTable = Collections.synchronizedMap(new HashMap<>());
    // Java objects created by calling a constructor in set statements. Messages
    // will be sent using
    // reflection.
    private Map<String, Object> javaReactors = Collections.synchronizedMap(new HashMap<>());
    private List<ActorRef<KlabMessage>> componentActors = Collections.synchronizedList(new ArrayList<>());
    private Layout layout;
    private Map<String, Library> libraries = new HashMap<>();
    private Map<String, Object> nativeLibraryInstances = new HashMap<>();

    /*
     * This is the parent that generated us through a 'new' instruction, if any.
     */
    private ActorRef<KlabMessage> parentActor = null;

    /*
     * if we pre-build actions or we run repeatedly we cache them here. Important that their run()
     * method is reentrant.
     */
    protected Map<String, KlabActionExecutor> actionCache = Collections.synchronizedMap(new HashMap<>());

    /*
     * actions that were created from system actions rather than actual actors, here so we can talk
     * to them from k.Actors
     */
    private Map<String, KlabActionExecutor.Actor> localActionExecutors = Collections.synchronizedMap(new HashMap<>());

    protected ActorRef<KlabMessage> getDispatcher() {
        if (this.appId == null) {
            return getContext().getSelf();
        }
        return ((ActorReference) identity.getActor()).actor;
    }

    /**
     * Descriptor for actions to be taken when a firing is recorded with the ID used as key in
     * matchActions.
     * 
     * @author Ferd
     */
    class MatchActions {

        ActorRef<KlabMessage> caller;
        List<Pair<Match, IKActorsStatement>> matches = new ArrayList<>();

        // this is the original calling scope, to use when the listening action is
        // executed upon a match.
        Scope scope;

        public void match(Object value, Map<String, Object> scopeVars) {

            for (Pair<Match, IKActorsStatement> match : matches) {

                if (match.getFirst().matches(value, scope)) {
                    Scope s = scope.withMatch(match.getFirst(), value, scopeVars);
                    s.symbolTable.putAll(symbolTable);
                    execute(match.getSecond(), s);
                    break;
                }
            }
        }

        public MatchActions(Scope scope) {
            this.scope = scope;
        }
    }

    /**
     * Runtime scope for all k.Actors statements. Root scopes are for each action. Local class so
     * that the identity is accessible.
     * 
     * @author Ferd
     */
    public static class Scope /* extends Parameters<String> */ implements IKActorsBehavior.Scope {

        boolean synchronous = false;
        Scope parent = null;
        IRuntimeScope runtimeScope;
        Long listenerId;
        IActorIdentity<KlabMessage> identity;
        Object match;
        String appId;
        public Map<String, Object> symbolTable = new HashMap<>();
        public Map<String, Object> globalSymbols;
        ViewScope viewScope;
        ActorRef<KlabMessage> sender;
        private boolean initializing;
        Semaphore semaphore = null;
        // metadata come with the actor specification if created through instantiation
        // and don't
        // change.
        Parameters<String> metadata;
        IBehavior behavior;

        /**
         * The scope is functional if an action that is declared as 'function' is called, or if the
         * executing action is part of a contextual chain (a1().a2().a3: ...). In this case any
         * "fire" statement does not fire a value but "returns" it, setting it in the scope and
         * breaking the execution.
         */
        boolean functional = false;

        /*
         * the following two support chaining of actions, with the ones before the last "returning"
         * values (may be defined using 'function' or be system actions) which end up in the scope
         * passed to the next. Because null is a legitimate value scope, we also use a boolean to
         * check if the scope contains a "context" value from a previous function.
         */
        boolean hasValueScope = false;
        Object valueScope = null;

        /**
         * Only instantiated in tests.
         */
        TestScope testScope;

        public Scope(IActorIdentity<KlabMessage> identity, String appId, IRuntimeScope scope, IBehavior behavior) {
            this.runtimeScope = scope;
            this.identity = identity;
            this.appId = appId;
            this.viewScope = new ViewScope(this);
            this.metadata = Parameters.create();
            this.behavior = behavior;
            if (behavior.getDestination() == Type.UNITTEST) {
                this.testScope = new TestScope(behavior);
            }
        }

        public Scope withGlobalSymbols(Map<String, Object> symbols) {
            this.symbolTable.putAll(symbols);
            if (this.globalSymbols == null) {
                this.globalSymbols = symbols;
            }
            return this;
        }

        public Scope withMatch(Match match, Object value, Map<String, Object> vars) {

            Scope ret = new Scope(this);

            ret.symbolTable.putAll(vars);

            /*
             * if we have identifiers either as key or in list key, match them to the values.
             * Otherwise match to $, $1, ... #n
             */
            if (match.isIdentifier(ret)) {
                ret.symbolTable.put(match.getIdentifier(), value);
            } else if (match.isImplicit()) {
                String matchId = match.getMatchName() == null ? "$" : match.getMatchName();
                ret.symbolTable.put(matchId, value);
                if (value instanceof Collection) {
                    int n = 1;
                    for (Object o : ((Collection<?>) value)) {
                        ret.symbolTable.put(matchId + (n++), o);
                    }
                }
            }
            ret.match = value;
            return ret;
        }

        public Scope(Scope scope) {
            this.globalSymbols = scope.globalSymbols;
            this.synchronous = scope.synchronous;
            this.runtimeScope = scope.runtimeScope;
            this.parent = scope;
            this.listenerId = scope.listenerId;
            this.sender = scope.sender;
            this.symbolTable.putAll(scope.symbolTable);
            this.identity = scope.identity;
            this.viewScope = scope.viewScope;
            this.appId = scope.appId;
            this.semaphore = scope.semaphore;
            this.metadata = scope.metadata;
            this.behavior = scope.behavior;
            // TODO check if we need to make a child and pass this
            this.testScope = scope.testScope;
        }

        public String toString() {
            return "{S " + listenerId + "}";
        }

        public Scope synchronous() {
            Scope ret = new Scope(this);
            ret.synchronous = true;
            return ret;
        }

        public Scope concurrent() {
            Scope ret = new Scope(this);
            ret.synchronous = false;
            return ret;
        }

        public Scope withNotifyId(Long id) {
            Scope ret = new Scope(this);
            ret.listenerId = id;
            return ret;
        }

        @Override
        public IIdentity getIdentity() {
            return identity;
        }

        public Long getNotifyId() {
            return listenerId;
        }

        @Override
        public boolean isSynchronous() {
            return this.synchronous;
        }

        @Override
        public Map<String, Object> getSymbolTable() {
            return this.symbolTable;
        }

        @Override
        public IMonitor getMonitor() {
            return this.runtimeScope == null ? null : this.runtimeScope.getMonitor();
        }

        public Scope withSender(ActorRef<KlabMessage> sender, String appId) {
            Scope ret = new Scope(this);
            ret.sender = sender;
            ret.appId = appId;
            return ret;
        }

        public Object getValue(String string) {
            if (symbolTable.containsKey(string)) {
                return symbolTable.get(string);
            } else if (globalSymbols != null && globalSymbols.containsKey(string)) {
                return globalSymbols.get(string);
            }
            return identity.getState().get(string, Object.class);
        }

        /**
         * Get a child scope for this action, which will create a panel viewscope if the action has
         * a view.
         * 
         * @param appId
         * @param action
         * @return
         */
        public Scope getChild(String appId, Action action) {
            Scope ret = new Scope(this);
            ret.viewScope = this.viewScope.getChild(action, appId, identity);
            return ret;
        }

        /**
         * Copy of scope with specialized variable value in symbol table.
         * 
         * @param variable
         * @param value
         * @return
         */
        public Scope withValue(String variable, Object value) {
            Scope ret = new Scope(this);
            ret.symbolTable.put(variable, value);
            return ret;
        }

        public Scope withComponent(ViewComponent component) {
            Scope ret = new Scope(this);
            ret.viewScope.currentComponent = component;
            return ret;
        }

        public Scope getChild(ConcurrentGroup code) {
            Scope ret = new Scope(this);
            if (!initializing && this.viewScope != null) {
                ret.viewScope = this.viewScope.getChild(code);
            }
            return ret;
        }

        public Map<String, Object> getSymbols(IActorIdentity<?> identity) {
            Map<String, Object> ret = new HashMap<>();
            ret.putAll(identity.getState());
            if (globalSymbols != null) {
                ret.putAll(globalSymbols);
            }
            ret.putAll(symbolTable);
            return ret;
        }

        public Scope forInit() {
            Scope ret = new Scope(this);
            ret.initializing = true;
            return ret;
        }

        public Scope forTest(Action action) {
            Scope ret = new Scope(this);
            ret.initializing = true;
            ret.synchronous = true;
            ret.testScope = ret.testScope.getChild(action);
            return ret;
        }

        public void waitForGreen(final int linenumber) {

            if (semaphore != null) {
                int cnt = 0;
                while(!Actors.INSTANCE.expired(semaphore)) {
                    try {
                        Thread.sleep(50);
                        cnt++;
                        if (cnt % 100 == 0 && !semaphore.isWarned()) {
                            identity.getMonitor().warn("Potential actor deadlock running " + getBehavior().getName() + ":"
                                    + linenumber + ": check logics");
                            semaphore.setWarned();
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }

        public Scope fence(boolean synchronize) {
            Scope ret = this;
            if (synchronize) {
                ret = new Scope(this);
                ret.semaphore = Actors.INSTANCE.createSemaphore(Semaphore.Type.FIRE);
            }
            return ret;
        }

        public Scope forWindow(IAnnotation wspecs, String actionId) {
            Scope ret = new Scope(this);
            ret.viewScope = ret.viewScope.createLayout(wspecs, actionId);
            return ret;
        }

        public Scope forAction(IBehavior.Action action) {
            return action.getStatement().isFunction() ? this : functional();
        }

        public Scope functional() {
            Scope ret = new Scope(this);
            ret.functional = true;
            return ret;
        }

        public Scope functional(Object valueScope) {
            Scope ret = new Scope(this);
            ret.functional = true;
            ret.valueScope = valueScope;
            return ret;
        }

        public Scope withReceiver(Object valueScope) {
            Scope ret = new Scope(this);
            ret.valueScope = valueScope;
            return ret;
        }

        public IBehavior getBehavior() {
            return this.behavior;
        }

        public Scope matchFormalArguments(Call code, Action actionCode) {

            Scope ret = this;

            if (!actionCode.getFormalArguments().isEmpty()) {
                ret = new Scope(this);
                int i = 0;
                for (String farg : actionCode.getFormalArguments()) {
                    Object value = null;
                    if (code.getArguments().getUnnamedArguments().size() > i) {
                        Object argument = code.getArguments().getUnnamedArguments().get(i);
                        value = argument instanceof KActorsValue
                                ? ((KActorsValue) argument).evaluate(this, identity, false)
                                : argument;
                    }
                    ret.symbolTable.put(farg, value);
                    i++;
                }
            }

            return ret;
        }

        public Scope withLayout(Layout layout) {
            if (this.viewScope != null) {
                this.viewScope.layout = layout;
            }
            return this;
        }

        public void onException(Throwable e, String message) {

            runtimeScope.getMonitor().error("actor exception: " + (message == null ? "" : message) + e.getMessage());

            if (testScope != null) {
                testScope.onException(e);
            }

        }

        public Scope getChild(IBehavior behavior) {
            Scope ret = new Scope(this);
            ret.behavior = behavior;
            if (this.testScope != null) {
                ret.testScope = ret.testScope.getChild(behavior);
            }
            return ret;
        }

    }

    protected IActorIdentity<KlabMessage> getIdentity() {
        return this.identity;
    }

    protected KlabActor(ActorContext<KlabMessage> context, IActorIdentity<KlabMessage> identity, String appId) {
        super(context);
        this.identity = identity;
        this.appId = appId;
    }

    /**
     * Basic messages. Redefine extending the result of super() to add.
     * 
     * @return a builder for the behavior
     */
    protected ReceiveBuilder<KlabMessage> configure() {
        ReceiveBuilder<KlabMessage> builder = newReceiveBuilder();
        return builder.onMessage(Load.class, this::handleLoadBehaviorMessage)
                .onMessage(Spawn.class, this::handleCreateChildMessage).onMessage(Fire.class, this::handleFireMessage)
                .onMessage(ComponentFire.class, this::handleComponentFireMessage)
                .onMessage(UserAction.class, this::handleUserActionMessage)
                .onMessage(UserMenuAction.class, this::handleMenuActionMessage).onMessage(AppReset.class, this::handleAppReset)
                .onMessage(AddComponentToGroup.class, this::handleAddComponentToGroupMessage)
                .onMessage(BindUserAction.class, this::handleBindActionMessage)
                .onMessage(KActorsMessage.class, this::handleCallMessage).onMessage(Stop.class, this::stopChild)
                .onMessage(Cleanup.class, this::handleCleanupMessage).onSignal(PostStop.class, signal -> onPostStop());
    }

    protected KlabActor onPostStop() {

        // TODO deactivate the underlying observation, send changes
        return this;
    }

    /**
     * Stop a child after cleaning up. Do nothing if child does not exist (may be leftover IDs from
     * a client connected to another engine).
     * 
     * @param message
     * @return
     */
    protected Behavior<KlabMessage> stopChild(Stop message) {

        if (message.appId != null && receivers.containsKey(message.appId)) {
            receivers.get(message.appId).tell(new Cleanup());
            getContext().stop(receivers.get(message.appId));
            receivers.remove(message.appId);
            return Behaviors.same();
        }

        return Behaviors.same();

    }

    protected Behavior<KlabMessage> handleAppReset(AppReset message) {

        // if (message.appId != null) {
        // ActorRef<KlabMessage> receiver = receivers.get(message.appId);
        // if (receiver != null) {
        // receiver.tell(message.direct());
        // }
        // } else {
        KActorsMessage mes = new KActorsMessage(getContext().getSelf(), "reset", null, null, message.scope, message.scope.appId);

        /**
         * FIXME not sure how this happens, but apparently it's only during debugging.
         */
        if (this.behavior != null) {

            /*
             * 1. call init and then reset if we are a component or an app and we have the actions
             */
            if (this.behavior.getDestination() == Type.APP || this.behavior.getDestination() == Type.COMPONENT) {
                for (IBehavior.Action action : this.behavior.getActions("init", "@init")) {
                    run(action, message.scope.getChild(this.appId, action));
                }
                for (IBehavior.Action action : this.behavior.getActions("reset", "@reset")) {
                    run(action, message.scope.getChild(this.appId, action));
                }
            }
        }

        /*
         * 2. send reset to all sub-actors that are components
         */
        for (ActorRef<KlabMessage> actor : componentActors) {
            actor.tell(message);
        }

        /*
         * 3. reset all UI components
         */
        for (KlabActionExecutor executor : actionCache.values()) {
            if (executor instanceof KlabWidgetActionExecutor) {
                ((KlabWidgetActionExecutor) executor).onMessage(mes, message.scope);
            }
        }

        /*
         * groups are handled separately and don't end up in the action cache
         */
        for (Actor executor : localActionExecutors.values()) {
            if (executor instanceof GroupHandler) {
                ((KlabWidgetActionExecutor) executor).onMessage(mes, message.scope);
            }
        }
        // }
        return Behaviors.same();
    }

    protected Behavior<KlabMessage> handleUserActionMessage(UserAction message) {

        if (message.appId != null) {
            ActorRef<KlabMessage> receiver = receivers.get(message.appId);
            if (receiver != null) {
                receiver.tell(message.direct());
            }
        } else {
            Long notifyId = this.actionBindings.get(message.action.getComponent().getId());
            if (notifyId != null && message.action.getComponent().getActorPath() == null) {
                MatchActions actions = listeners.get(notifyId);
                if (actions != null) {
                    KlabActionExecutor executor = actionCache.get(message.action.getComponent().getId());
                    actions.match(
                            executor instanceof KlabWidgetActionExecutor
                                    ? ((KlabWidgetActionExecutor) executor).getFiredValue(message.action,
                                            new Scope(identity, appId, message.scope, this.behavior)
                                                    .withGlobalSymbols(this.symbolTable))
                                    : getActionValue(message.action),
                            message.scope);
                }
            } else if (message.action.getComponent().getActorPath() != null) {

                // dispatch to child actor
                String path = message.action.getComponent().getActorPath();
                String[] elements = path.split("\\.");
                if (elements.length > 0) {

                    String actorId = elements[0];
                    ActorRef<KlabMessage> receiver = receivers.get(actorId);
                    if (receiver != null) {
                        if (elements.length == 1) {
                            message.action.getComponent().setActorPath(null);
                        } else if (elements.length > 1) {
                            message.action.getComponent().setActorPath(Path.getRemainder(path, "."));
                        }
                        receiver.tell(message);
                    } else {
                        message.scope.getMonitor()
                                .error("unreferenced child actor " + actorId + " when handling message from UI");
                    }
                }
            }
        }
        return Behaviors.same();
    }

    protected Behavior<KlabMessage> handleMenuActionMessage(UserMenuAction message) {

        if (message.appId != null) {
            ActorRef<KlabMessage> receiver = receivers.get(message.appId);
            if (receiver != null) {
                receiver.tell(message.direct());
            }
        } else {

            String actionId = message.action.getMenuId();
            if (actionId.startsWith("menu.")) {
                actionId = actionId.substring(5);
            }

            Action actionCode = behavior.getAction(actionId);
            if (actionCode != null) {
                Scope scope = new Scope(identity, appId, message.scope, this.behavior).withLayout(this.layout)
                        .withGlobalSymbols(this.symbolTable);
                run(actionCode, scope);
            }

            // Long notifyId =
            // this.actionBindings.get(message.action.getComponent().getId());
            // if (notifyId != null && message.action.getComponent().getActorPath() == null)
            // {
            // MatchActions actions = listeners.get(notifyId);
            // if (actions != null) {
            // KlabActionExecutor executor =
            // actionCache.get(message.action.getComponent().getId());
            // actions.match(
            // executor instanceof KlabWidgetActionExecutor
            // ? ((KlabWidgetActionExecutor) executor).getFiredValue(message.action,
            // new Scope(identity, appId, message.scope, this.behavior)
            // .withGlobalSymbols(this.symbolTable))
            // : getActionValue(message.action),
            // message.scope);
            // }
            // } else if (message.action.getComponent().getActorPath() != null) {
            //
            // // dispatch to child actor
            // String path = message.action.getComponent().getActorPath();
            // String[] elements = path.split("\\.");
            // if (elements.length > 0) {
            //
            // String actorId = elements[0];
            // ActorRef<KlabMessage> receiver = receivers.get(actorId);
            // if (receiver != null) {
            // if (elements.length == 1) {
            // message.action.getComponent().setActorPath(null);
            // } else if (elements.length > 1) {
            // message.action.getComponent().setActorPath(Path.getRemainder(path, "."));
            // }
            // receiver.tell(message);
            // } else {
            // message.scope.getMonitor()
            // .error("unreferenced child actor " + actorId + " when handling message from
            // UI");
            // }
            // }
            // }
        }
        return Behaviors.same();
    }

    protected Behavior<KlabMessage> handleCleanupMessage(Cleanup message) {

        /*
         * this may intercept listeners
         */
        for (KlabActionExecutor action : actionCache.values()) {
            action.dispose();
        }

        return this;
    }

    public static Object getActionValue(ViewAction action) {
        if (action.isBooleanValue() != null) {
            return action.isBooleanValue();
        } else if (action.getStringValue() != null) {
            return action.getStringValue();
        } else if (action.getDateValue() != null) {
            return action.getDoubleValue();
        } else if (action.getDoubleValue() != null) {
            return action.getDoubleValue();
        } else if (action.getIntValue() != null) {
            // ACHTUNG Jackson sticks a 0 in here even if the incoming JSON has null. MUST
            // keep this check last!
            return action.getIntValue();
        }
        return null;
    }

    @Override
    final public Receive<KlabMessage> createReceive() {
        return configure().build();
    }

    /*
     * ----------------------------------------------------------------------- k.Actors interpreter
     * -----------------------------------------------------------------------
     */

    protected void run(IBehavior.Action action, Scope scope) {

        IAnnotation wspecs = Annotations.INSTANCE.getAnnotation(action, "modal");
        if (wspecs == null) {
            wspecs = Annotations.INSTANCE.getAnnotation(action, "window");
        }

        if (wspecs != null) {
            scope = scope.forWindow(wspecs, action.getName());
        }

        if (action.isFunction()) {
            scope = scope.functional();
        }

        try {

            execute(action.getStatement().getCode(), scope.forAction(action));

        } catch (Throwable t) {

            scope.onException(t, "action " + action.getBehavior() + " " + action.getName());

            if (scope.sender != null) {
                scope.sender.tell(new Fire(scope.listenerId, t, scope.appId, scope.semaphore, scope.getSymbols(this.identity)));
            } else if (parentActor != null) {

                /*
                 * No sender = the fire is not coming from an internal action but goes out to the
                 * world, which in this case is the parent actor. Let our parent know we've fired
                 * with a message carrying the name it knows us by, so that the value can be matched
                 * to what is caught after the 'new' verb. Listener ID is the actor's name.
                 */
                parentActor.tell(new ComponentFire(getContext().getSelf().path().name(), t, getContext().getSelf()));

            } else {

                /*
                 * Fart in space: nothing is listening from the behavior being executed. TODO - an
                 * actor firing with no action listening and no parent should just send to either
                 * the user actor or (maybe) its parent identity? TODO - the outer group may be
                 * listening.
                 */
            }
        }

        if (wspecs != null) {
            if (Configuration.INSTANCE.isEchoEnabled()) {
                System.out.println(Actors.INSTANCE.dumpView(scope.viewScope.layout));
            }
            KlabActor.this.identity.setView(new ViewImpl(scope.viewScope.layout));
            KlabActor.this.identity.getMonitor().send(IMessage.MessageClass.UserInterface,
                    "modal".equals(wspecs.getName()) ? IMessage.Type.CreateModalWindow : IMessage.Type.CreateWindow,
                    scope.viewScope.layout);
        }
    }

    private boolean execute(IKActorsStatement code, Scope scope) {

        try {
            switch(code.getType()) {
            case ACTION_CALL:
                executeCall((IKActorsStatement.Call) code, scope);
                break;
            case ASSIGNMENT:
                executeAssignment((IKActorsStatement.Assignment) code, scope);
                break;
            case DO_STATEMENT:
                executeDo((IKActorsStatement.Do) code, scope);
                break;
            case FIRE_VALUE:
                return executeFire((IKActorsStatement.FireValue) code, scope);
            case FOR_STATEMENT:
                executeFor((IKActorsStatement.For) code, scope);
                break;
            case IF_STATEMENT:
                executeIf((IKActorsStatement.If) code, scope);
                break;
            case CONCURRENT_GROUP:
                executeGroup((IKActorsStatement.ConcurrentGroup) code, scope);
                break;
            case SEQUENCE:
                executeSequence((IKActorsStatement.Sequence) code, scope);
                break;
            case TEXT_BLOCK:
                executeText((IKActorsStatement.TextBlock) code, scope);
                break;
            case WHILE_STATEMENT:
                executeWhile((IKActorsStatement.While) code, scope);
                break;
            case INSTANTIATION:
                executeInstantiation((IKActorsStatement.Instantiation) code, scope);
                break;
            case ASSERT_STATEMENT:
                executeAssert((IKActorsStatement.Assert) code, scope);
                break;
            default:
                break;
            }
        } catch (Throwable t) {
            Logging.INSTANCE.warn("Exception thrown in k.Actors interpreter: " + t.getMessage());
        }

        return true;
    }

    private void executeInstantiation(Instantiation code, Scope scope) {

        Behavior<KlabMessage> child = null;
        if (this.identity instanceof Observation) {
            child = ObservationActor.create((Observation) this.identity, null);
        } else if (this.identity instanceof Session) {
            /**
             * TODO if the actor has a view, use a behavior can address enable/disable/hide messages
             * and the like.
             */
            child = SessionActor.create((Session) this.identity, null);
        } else if (this.identity instanceof EngineUser) {
            child = UserActor.create((EngineUser) this.identity);
        }

        // existing actors for this behavior
        List<ActorRef<KlabMessage>> actors = this.childInstances.get(code.getActorBaseName());
        String actorName = code.getActorBaseName() + (actors == null ? "" : ("_" + (actors.size() + 1)));

        ActorRef<KlabMessage> actor = getContext()
                .spawn(Behaviors.supervise(child).onFailure(SupervisorStrategy.resume().withLoggingEnabled(true)), actorName);

        /*
         * use the actor name to install a listener for any actions that may be connected to this
         * instance; it will be used as listener ID for the ComponentFire message sent when the
         * child fires.
         */
        if (code.getActions().size() > 0) {

            MatchActions actions = new MatchActions(scope);
            for (Triple<IKActorsValue, IKActorsStatement, String> adesc : code.getActions()) {
                actions.matches.add(
                        new Pair<Match, IKActorsStatement>(new Match(adesc.getFirst(), adesc.getThird()), adesc.getSecond()));
            }
            this.componentFireListeners.put(actorName, actions);
        }

        // remove the appId for the children, otherwise their messages will be rerouted
        Map<String, Object> arguments = new HashMap<>();
        Map<String, Object> metadata = new HashMap<>();
        if (code.getArguments() != null) {
            /*
             * TODO match the arguments to the correspondent names for the declaration of main()
             */
            IBehavior childBehavior = Actors.INSTANCE.getBehavior(code.getBehavior());
            if (childBehavior == null) {
                this.getIdentity().getMonitor()
                        .error("unreferenced child behavior: " + code.getBehavior() + " when execute instantiation");
                return;
            }
            Action main = childBehavior.getAction("main");
            int n = 0;
            for (int i = 0; i < main.getStatement().getArgumentNames().size(); i++) {
                String arg = main.getStatement().getArgumentNames().get(i);
                Object value = code.getArguments().get(arg);
                if (value == null && code.getArguments().getUnnamedKeys().size() > n) {
                    value = code.getArguments().get(code.getArguments().getUnnamedKeys().get(n++));
                    if (value instanceof KActorsValue) {
                        value = ((KActorsValue) value).evaluate(scope, identity, false);
                    }
                }
                arguments.put(arg, value);
            }
            for (String arg : ((KActorsArguments) code.getArguments()).getMetadataKeys()) {
                Object value = code.getArguments().get(arg);
                if (value instanceof KActorsValue) {
                    value = ((KActorsValue) value).evaluate(scope, identity, false);
                }
                metadata.put(arg, value);
            }
        }

        IBehavior actorBehavior = Actors.INSTANCE.getBehavior(code.getBehavior());
        if (actorBehavior != null) {

            /*
             * AppID in message is null because this is run by the newly spawned actor; we
             * communicate the overall appID through the specific field below.
             */
            Load loadMessage = new Load(this.identity, code.getBehavior(), null, scope)
                    .withChildActorPath(this.childActorPath == null ? actorName : (this.childActorPath + "." + actorName))
                    .withActorBaseName(code.getActorBaseName()).withMainArguments(arguments).withMetadata(metadata)
                    .withApplicationId(this.appId).withParent(getContext().getSelf());

            Semaphore semaphore = null;
            if (actorBehavior.getDestination() == Type.COMPONENT) {
                /*
                 * synchronize by default
                 */
                semaphore = Actors.INSTANCE.createSemaphore(Semaphore.Type.LOAD);
                loadMessage.withSemaphore(semaphore);
                componentActors.add(actor);
            }

            actor.tell(loadMessage);

            if (semaphore != null) {
                waitForGreen(semaphore);
            }

            receivers.put(actorName, actor);

            if (actors == null) {
                actors = new ArrayList<>();
                this.childInstances.put(actorName, actors);
            }

            actors.add(actor);
        }
    }

    private void waitForGreen(Semaphore semaphore) {

        while(!Actors.INSTANCE.expired(semaphore)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void executeWhile(While code, Scope scope) {
        // TODO Auto-generated method stub

    }

    private void executeText(TextBlock code, Scope scope) {
        executeCall(new KActorsActionCall(code), scope);
    }

    private void executeSequence(Sequence code, Scope scope) {
        if (code.getStatements().size() == 1) {
            execute(code.getStatements().get(0), scope);
        } else {
            for (IKActorsStatement statement : code.getStatements()) {
                if (!execute(statement, scope.synchronous())) {
                    break;
                }
                // TODO waitForCompletion(message);
            }
        }
    }

    private void executeGroup(ConcurrentGroup code, Scope scope) {
        Scope groupScope = scope.getChild(code);
        if (code.getTag() != null) {
            /*
             * install executor for group actions
             */
            this.localActionExecutors.put(code.getTag(),
                    new GroupHandler(this.identity, appId, groupScope, this.getContext().getSelf(), null));
        }
        for (IKActorsStatement statement : code.getStatements()) {
            if (!execute(statement, groupScope)) {
                break;
            }
        }
    }

    private void executeIf(If code, Scope scope) {

        Object check = ((KActorsValue) code.getCondition()).evaluate(scope, identity, true);
        if (KActorsValue.isTrue(check)) {
            if (code.getThen() != null) {
                execute(code.getThen(), scope);
            }
        } else {
            for (Pair<IKActorsValue, IKActorsStatement> conditions : code.getElseIfs()) {
                check = ((KActorsValue) conditions.getFirst()).evaluate(scope, identity, true);
                if (KActorsValue.isTrue(check)) {
                    execute(conditions.getSecond(), scope);
                    return;
                }
            }
            if (code.getElse() != null) {
                execute(code.getElse(), scope);
            }
        }

    }

    private void executeFor(For code, Scope scope) {
        for (Object o : Actors.INSTANCE.getIterable(code.getIterable(), scope, identity)) {
            // TODO handle break action - needs a statement, then separate the scope and
            // check for
            // break at each iteration
            if (!execute(code.getBody(), scope.withValue(code.getVariable(), o))) {
                break;
            }
        }
    }

    private void executeAssert(Assert code, Scope scope) {
        for (Assertion assertion : code.getAssertions()) {
            executeCallChain(assertion.getCalls(), scope);
            if (assertion.getValue() != null) {
                TestBehavior.evaluateAssertion(scope.valueScope, assertion, scope);
            }
        }
    }

    private static Object executeFunctionChain(List<Call> functions, Scope scope) {
        Object contextReceiver = null;
        for (int i = 0; i < functions.size(); i++) {
            boolean last = (i == functions.size() - 1);
            Scope fscope = last ? scope.withReceiver(contextReceiver) : scope.functional(contextReceiver);
            callFunctionOrMethod(functions.get(i), fscope);
            contextReceiver = fscope.valueScope;
        }
        return contextReceiver;
    }

    /**
     * If the call is a known function, call it and leave the value in the scope. Otherwise check if
     * it's a method of the valueScope receiver if we have it.
     * 
     * @param call
     * @param fscope
     */
    private static void callFunctionOrMethod(Call call, Scope fscope) {
        // TODO Auto-generated method stub

    }

    /**
     * A call sequence is a one or more calls to be executed in sequence. The last call is a
     * standard message call which will either fire or return according to the scope; the ones
     * preceding it, if any, are necessarily functional and the return value of the first provides
     * the execution context for the next.
     * 
     * @param calls
     * @param scope
     */
    private void executeCallChain(List<Call> calls, Scope scope) {

        Object contextReceiver = null;
        for (int i = 0; i < calls.size(); i++) {
            boolean last = (i == calls.size() - 1);
            Scope fscope = last ? scope.withReceiver(contextReceiver) : scope.functional(contextReceiver);
            executeCall(calls.get(i), fscope);
            contextReceiver = fscope.valueScope;
        }
        scope.valueScope = contextReceiver;
    }

    /**
     * TODO add handling of test cases - all fires (including exceptions) should be intercepted
     * 
     * @param code
     * @param scope\
     * @return false if the scope is functional and execution should stop.
     */
    private boolean executeFire(FireValue code, Scope scope) {

        if (scope.functional) {
            scope.hasValueScope = true;
            scope.valueScope = code.getValue().evaluate(scope, identity, false);
            return false;
        }

        if (scope.getNotifyId() != null) {
            // TODO don't think this is handled properly. Should catch its own fire in the
            // behavior - right?
            System.err.println("NOTIFY ID NON-NULL IN FIRE FROM ACTION - ENSURE IT'S HANDLED");
        }

        if (scope.sender != null) {

            /*
             * this should happen when a non-main action executes the fire. Must be checked first.
             * Fire may happen if the action firing is called again, so don't remove the listener.
             */
            scope.sender.tell(new Fire(scope.listenerId, code.getValue().evaluate(scope, identity, false), scope.appId,
                    scope.semaphore, scope.getSymbols(this.identity)));

        } else if (parentActor != null) {

            /*
             * No sender = the fire is not coming from an internal action but goes out to the world,
             * which in this case is the parent actor. Let our parent know we've fired with a
             * message carrying the name it knows us by, so that the value can be matched to what is
             * caught after the 'new' verb. Listener ID is the actor's name.
             */
            parentActor.tell(new ComponentFire(getContext().getSelf().path().name(),
                    code.getValue().evaluate(scope, identity, false), getContext().getSelf()));

        } else {

            /*
             * Fart in space: nothing is listening from the behavior being executed. TODO - an actor
             * firing with no action listening and no parent should just send to either the user
             * actor or (maybe) its parent identity? TODO - the outer group may be listening.
             */

        }

        return true;
    }

    private void executeDo(Do code, Scope scope) {
        // TODO Auto-generated method stub

    }

    private void executeAssignment(Assignment code, Scope scope) {
        if (code.getRecipient() != null) {
            if ("self".equals(code.getRecipient())) {
                this.identity.getState().put(code.getVariable(),
                        ((KActorsValue) code.getValue()).evaluate(scope, identity, false));
            } else {
                // TODO find the actor reference and send it an internal message to set the
                // state. Should be subject to scope and authorization
                throw new KlabUnimplementedException("klab actor state setting is unimplemented");
            }
        } else if (((KActorsValue) code.getValue()).getConstructor() != null) {

            Object o = ((KActorsValue) code.getValue()).evaluate(scope, identity, false);
            this.javaReactors.put(code.getVariable(), o);
            this.symbolTable.put(code.getVariable(), o);

        } else {
            // set goes into the actor's symbol table, only parameters can override it.
            this.symbolTable.put(code.getVariable(), ((KActorsValue) code.getValue()).evaluate(scope, identity, false));
        }
    }

    // protected static Object evaluate(Object value, Scope scope) {
    // if (value instanceof KActorsValue) {
    // return evaluateInScope((KActorsValue) value, scope, scope.identity);
    // }
    // return value;
    // }

    // protected Object evaluateInScope(KActorsValue arg, Scope scope) {
    // return evaluateInScope(arg, scope, this.identity);
    // }

    @SuppressWarnings("unchecked")
    public static Object evaluateInScope(KActorsValue arg, Scope scope, IActorIdentity<?> identity) {

        Object ret = null;

        switch(arg.getType()) {
        case OBJECT:
            ret = Actors.INSTANCE.createJavaObject(arg.getConstructor(), scope, identity);
            break;
        case COMPONENT:
            ret = arg.getConstructor();
            break;
        case QUANTITY:
            ret = arg.getStatedValue();
            break;
        case OBSERVABLE:
            if (arg.getData() instanceof IObservable) {
                ret = arg.getData();
            } else if (arg.getStatedValue() instanceof IKimObservable) {
                ret = Observables.INSTANCE.declare((IKimObservable) arg.getStatedValue(), identity.getMonitor());
                arg.setData(ret);
            }
            break;
        case ERROR:
            throw arg.getStatedValue() instanceof Throwable
                    ? new KlabException((Throwable) arg.getStatedValue())
                    : new KlabException(arg.getStatedValue() == null
                            ? "Unspecified actor error from error value"
                            : arg.getStatedValue().toString());

        case NUMBERED_PATTERN:

            if (!"$".equals(arg.getStatedValue().toString())) {
                // TODO
            } /* else fall through to IDENTIFIER */

        case IDENTIFIER:

            // TODO check for recipient in ID
            ret = scope.getValue(arg.getStatedValue().toString());
            break;

        case EXPRESSION:

            if (arg.getData() == null) {

                Object val = arg.getStatedValue();
                if (val instanceof String) {
                    val = Extensions.INSTANCE.parse((String) val);
                }

                arg.setData(new ObjectExpression((IKimExpression) val, scope.runtimeScope, CompilerOption.WrapParameters,
                        CompilerOption.IgnoreContext));
            }

            try {
                /*
                 * 'metadata' is bound to the actor metadata map, initialized in the call
                 */
                ret = ((ObjectExpression) arg.getData()).eval(scope.runtimeScope, identity,
                        Parameters.create(scope.getSymbols(identity), "metadata", scope.metadata, "self", identity));
            } catch (Throwable t) {
                scope.getMonitor().error(t);
                return null;
            }

            break;

        case OBSERVATION:
            // TODO
            break;
        case SET:
            // TODO eval all args
            break;
        case LIST:
            ret = new ArrayList<Object>();
            for (Object o : (Collection<?>) arg.getStatedValue()) {
                ((List<Object>) ret).add(o instanceof KActorsValue ? evaluateInScope((KActorsValue) o, scope, identity) : o);
            }
            break;
        case TREE:
            // TODO eval all args
            break;
        case MAP:
            // TODO eval all args
            break;
        case TABLE:
            // TODO eval all args
            break;
        case URN:
            ret = new Urn(arg.getStatedValue().toString());
            break;
        case CALLCHAIN:
            ret = executeFunctionChain(arg.getCallChain(), scope);
            break;
        default:
            ret = arg.getStatedValue();
        }

        if (arg.getExpressionType() == ExpressionType.TERNARY_OPERATOR) {
            if (Actors.INSTANCE.asBooleanValue(ret)) {
                ret = arg.getTrueCase() == null ? null : evaluateInScope(arg.getTrueCase(), scope, identity);
            } else {
                ret = arg.getFalseCase() == null ? null : evaluateInScope(arg.getFalseCase(), scope, identity);
            }
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    private void executeCall(Call code, Scope scope) {

        Long notifyId = scope.listenerId;

        /**
         * Exec any calls that precede this one, so that the receiver is set
         */
        Object contextReceiver = null;
        for (Call chained : code.getChainedCalls()) {
            Scope fscope = scope.functional(contextReceiver);
            executeCall(code, fscope);
            contextReceiver = fscope.valueScope;
        }

        boolean synchronize = false;

        if (code.getActions().size() > 0) {

            synchronize = scope.synchronous;

            notifyId = nextId.incrementAndGet();
            MatchActions actions = new MatchActions(scope);
            for (Triple<IKActorsValue, IKActorsStatement, String> adesc : code.getActions()) {
                actions.matches.add(
                        new Pair<Match, IKActorsStatement>(new Match(adesc.getFirst(), adesc.getThird()), adesc.getSecond()));
            }
            this.listeners.put(notifyId, actions);
        }

        if (code.getGroup() != null) {
            // TODO finish handling group actions
            execute(code.getGroup(), scope.withNotifyId(notifyId));
            return;
        }

        String receiverName = "self";
        String messageName = code.getMessage();
        if (messageName.contains(".")) {
            receiverName = Path.getLeading(messageName, '.');
            messageName = Path.getLast(messageName, '.');
        }

        if (!"self".equals(receiverName)) {

            /*
             * Check first if the recipient is a Java peer and in that case, use reflection to send
             * it the message and return.
             */
            if (this.javaReactors.containsKey(receiverName)
                    || (scope.symbolTable.containsKey(receiverName) && !Utils.isPOD(scope.symbolTable.get(receiverName)))
                    || (scope.globalSymbols.containsKey(receiverName) && !Utils.isPOD(scope.globalSymbols.get(receiverName)))) {

                Object reactor = this.javaReactors.get(receiverName);
                if (reactor == null) {
                    reactor = scope.symbolTable.get(receiverName);
                }
                if (reactor == null) {
                    reactor = scope.globalSymbols.get(receiverName);
                }
                if (reactor != null) {
                    Actors.INSTANCE.invokeReactorMethod(reactor, messageName, code.getArguments(), scope, this.identity);
                }

                return;
            }

            /*
             * Check if the name corresponds to the tag of an executor created using new. If so, the
             * actor (or component) has priority over a possible actor of the same name or a
             * variable containing an actor.
             */
            if (this.localActionExecutors.containsKey(receiverName)) {
                KActorsMessage m = new KActorsMessage(getContext().getSelf(), messageName, code.getCallId(), code.getArguments(),
                        scope.withNotifyId(notifyId), appId);
                this.localActionExecutors.get(receiverName).onMessage(m, scope);
                scope.waitForGreen(code.getFirstLine());
                return;
            }

            /*
             * Otherwise, an actor reference with this local name may have been passed as a
             * parameter or otherwise set in the symbol table as a variable.
             */
            ActorRef<KlabMessage> recipient = null;
            if (scope.symbolTable.get(receiverName) instanceof IActorIdentity) {
                try {
                    recipient = ((ActorReference) ((IActorIdentity<KlabMessage>) scope.symbolTable.get(receiverName))
                            .getActor()).actor;
                } catch (Throwable t) {
                    // TODO do something with the failed call, the actor should probably remember
                    if (this.identity instanceof IRuntimeIdentity) {
                        ((IRuntimeIdentity) this.identity).getMonitor()
                                .error("error executing actor call " + messageName + ": " + t.getMessage());
                    }
                    return;
                }
            } /* TODO check if it's a library! */ else {
                /*
                 * Only remaining choice for an explicit actor name must be in the recipient table.
                 */
                recipient = receivers.get(receiverName);
            }

            if (recipient == null) {
                /*
                 * No recipient, we just set this to the user actor which will turn the message into
                 * whatever is set for unknown messages. This not returning null guarantees that the
                 * message will arrive.
                 */
                recipient = ((ActorReference) (identity.getParentIdentity(EngineUser.class).getActor())).actor;
            }

            if (synchronize) {
                scope.runtimeScope.getMonitor()
                        .warn("External actor calls are being made within a synchronous scope: this should "
                                + " never happen. The synchronization is being ignored.");
            }

            recipient.tell(new KActorsMessage(getContext().getSelf(), messageName, code.getCallId(), code.getArguments(),
                    scope.withNotifyId(notifyId), appId));

            return;

        }

        Action libraryActionCode = null;

        /*
         * check if the call is a method from the library and if it applies to the context receiver
         * in case we have one.
         */
        for (Library library : libraries.values()) {
            if (library.methods.containsKey(messageName)) {

                CallDescriptor method = library.methods.get(messageName);
                if (method.method != null) {

                    if (scope.valueScope != null) {

                        /*
                         * must be compatible with the same argument of the method; otherwise we
                         * continue on to receiver call.
                         */
                        boolean ok = method.method.getParameterCount() > 0
                                && scope.valueScope.getClass().isAssignableFrom(method.method.getParameters()[0].getType());

                        if (!ok) {
                            continue;
                        }

                    }

                    /*
                     * run through reflection and set the value scope to the result
                     */
                    List<Object> args = new ArrayList<>();
                    for (Object arg : code.getArguments().getUnnamedArguments()) {
                        args.add(arg instanceof KActorsValue ? evaluateInScope((KActorsValue) arg, scope, identity) : arg);
                    }
                    try {
                        scope.valueScope = method.method.invoke(nativeLibraryInstances.get(library.name), args.toArray());
                        return;
                    } catch (Throwable e) {
                        throw new KlabActorException(e);
                    }

                } else {

                    /*
                     * TODO it's an action from a k.Actors-specified library - just set it as the
                     * value of actionCode. It may be functional or not.
                     */
                }
            }
        }

        /*
         * at this point if we have a valueScope, we are calling a method on it.
         */
        if (scope.valueScope != null) {
            scope.valueScope = Actors.INSTANCE.invokeReactorMethod(scope.valueScope, messageName, code.getArguments(), scope,
                    identity);
            return;
        }

        /*
         * If we get here, the message is directed to self and it may specify an executor or a
         * k.Actors behavior action. A coded action takes preference over a system behavior
         * executor.
         * 
         * Now we're in the appropriate scope for synchronous execution if we have actions after
         * fire.
         */
        scope = scope.fence(synchronize);

        // TODO if libraryActionCode is not null, we should override only if the library wasn't
        // explicitly
        // stated.
        Action actionCode = behavior.getAction(messageName);
        if (actionCode != null || libraryActionCode != null) {
            /*
             * local action overrides a library action
             */
            run(actionCode, scope.matchFormalArguments(code, (actionCode == null ? libraryActionCode : actionCode))
                    .withNotifyId(notifyId));
            return;
        }

        String executorId = (this.childActorPath == null ? "" : (this.childActorPath + "_")) + code.getCallId();

        /*
         * Remaining option is a code action executor installed through a system behavior. The
         * executor cache is populated at every execution of the same call, so this will be
         * instantiated only if the call has been executed before (in a loop or upon repeated calls
         * of the same action).
         */
        KlabActionExecutor executor = actionCache.get(executorId);

        if (executor == null) {
            Class<? extends KlabActionExecutor> actionClass = Actors.INSTANCE.getActionClass(messageName);
            if (actionClass != null) {

                executor = Actors.INSTANCE.getSystemAction(messageName, this.getIdentity(), code.getArguments(),
                        scope.withNotifyId(notifyId), getContext().getSelf(), executorId);

                if (executor != null) {

                    actionCache.put(executorId, executor);

                    if (executor instanceof KlabActionExecutor.Actor) {

                        /*
                         * if it has a tag, store for later reference.
                         */
                        if (code.getArguments().containsKey("tag")) {
                            Object t = code.getArguments().get("tag");
                            if (t instanceof KActorsValue) {
                                t = ((KActorsValue) t).evaluate(scope, identity, true);
                            }
                            ((KlabActionExecutor.Actor) executor).setName(t.toString());
                            this.localActionExecutors.put(((KlabActionExecutor.Actor) executor).getName(),
                                    (KlabActionExecutor.Actor) executor);
                        }
                    }

                    /*
                     * if there are actions, set the bindings
                     */
                    if (code.getActions().size() > 0) {
                        this.actionBindings.put(executorId, notifyId);
                    }
                }
            }
        }

        if (executor instanceof KlabWidgetActionExecutor) {

            /*
             * the run() method in these is never called: they act through their view components
             */
            ViewComponent viewComponent = ((KlabWidgetActionExecutor) executor).getViewComponent();

            // may be null if the addition of the component happens as the result of an
            // action
            // enqueued by the component on this actor, run and notified by the message
            // handler
            // after the call.
            if (viewComponent != null) {
                scope.viewScope.setViewMetadata(viewComponent, executor.arguments);
                viewComponent.setIdentity(this.identity.getId());
                viewComponent.setApplicationId(this.appId);
                viewComponent.setParentId(code.getCallId()); // check - seems
                                                             // wrong
                viewComponent.setId(executorId);
                viewComponent.setActorPath(this.childActorPath);
                ((KlabWidgetActionExecutor) executor).setInitializedComponent(viewComponent);
                scope.viewScope.currentComponent.getComponents().add(viewComponent);
            }

        } else if (executor != null) {
            executor.run(scope.withNotifyId(notifyId).withSender(getContext().getSelf(), appId));
        }

        /*
         * if the scope was not synchronous, or there were no actions after a fire, this does
         * nothing.
         */
        scope.waitForGreen(code.getFirstLine());

    }

    /*
     * ----------------------------------------------------------------------- k.Actors behavior
     * -----------------------------------------------------------------------
     */

    protected Behavior<KlabMessage> handleBindActionMessage(BindUserAction message) {
        if (message.appId != null) {
            ActorRef<KlabMessage> receiver = receivers.get(message.appId);
            if (receiver != null) {
                receiver.tell(message.direct());
            }
        } else {
            this.actionBindings.put(message.componentId, message.notifyId);
        }
        return Behaviors.same();
    }

    @SuppressWarnings("unchecked")
    protected Behavior<KlabMessage> handleComponentFireMessage(ComponentFire message) {
        if (message.listenerId != null) {
            MatchActions actions = componentFireListeners.get(message.listenerId);
            if (actions != null) {
                actions.match(message.value, MapUtils.EMPTY_MAP);
            }
        }
        return Behaviors.same();
    }

    protected Behavior<KlabMessage> handleAddComponentToGroupMessage(AddComponentToGroup message) {

        /*
         * this is only for components, and these only happen in sessions.
         */
        if (!(this.identity instanceof Session)) {
            return Behaviors.same();
        }

        Behavior<KlabMessage> child = SessionActor.create((Session) this.identity, null);
        String actorName = message.group.getId().replaceAll("/", "_") + "_" + message.id;

        // existing actors for this behavior
        List<ActorRef<KlabMessage>> actors = this.childInstances.get(appId);

        // TODO detach so that the components can be written to this without consequence
        Scope scope = message.scope.withComponent(message.group);

        ActorRef<KlabMessage> actor = getContext()
                .spawn(Behaviors.supervise(child).onFailure(SupervisorStrategy.resume().withLoggingEnabled(true)), actorName);

        // remove the appId for the children, otherwise their messages will be rerouted
        Map<String, Object> arguments = new HashMap<>();
        if (message.arguments != null) {
            /*
             * TODO match the arguments to the correspondent names for the declaration of main()
             */
            IBehavior childBehavior = Actors.INSTANCE.getBehavior(message.componentPath);
            if (childBehavior == null) {
                this.getIdentity().getMonitor()
                        .error("unreferenced child behavior: " + message.componentPath + " when execute instantiation");
                return Behaviors.same();
            }
            Action main = childBehavior.getAction("main");
            int n = 0;
            for (int i = 0; i < main.getStatement().getArgumentNames().size(); i++) {
                String arg = main.getStatement().getArgumentNames().get(i);
                Object value = message.arguments.get(arg);
                if (value == null && message.arguments.getUnnamedKeys().size() > n) {
                    value = message.arguments.get(message.arguments.getUnnamedKeys().get(n++));
                    if (value instanceof KActorsValue) {
                        value = ((KActorsValue) value).evaluate(scope, identity, false);
                    }
                }
                arguments.put(arg, value);
            }
        }

        IBehavior actorBehavior = Actors.INSTANCE.getBehavior(message.componentPath);
        if (actorBehavior != null) {

            /*
             * AppID in message is null because this is run by the newly spawned actor; we
             * communicate the overall appID through the specific field below.
             */
            Load loadMessage = new Load(this.identity, message.componentPath, null, scope)
                    .withChildActorPath(this.childActorPath == null ? actorName : (this.childActorPath + "." + actorName))
                    .withActorBaseName(actorName).withMainArguments(arguments).withApplicationId(this.appId)
                    .withParent(getContext().getSelf());

            Semaphore semaphore = null;
            if (actorBehavior.getDestination() == Type.COMPONENT) {
                /*
                 * synchronize by default
                 */
                semaphore = Actors.INSTANCE.createSemaphore(Semaphore.Type.LOAD);
                loadMessage.withSemaphore(semaphore);
                componentActors.add(actor);
            }

            actor.tell(loadMessage);

            if (semaphore != null) {

                waitForGreen(semaphore);

                /*
                 * TODO send the view message to the view with the group definition from the view
                 * scope
                 */
                ViewAction action = new ViewAction(scope.viewScope.currentComponent);
                action.setApplicationId(appId);
                action.setData(ViewBehavior.getMetadata(message.arguments, scope));
                // action.setComponentTag(this.getName());
                // session.getState().updateView(this.component);
                identity.getMonitor().send(IMessage.MessageClass.ViewActor, IMessage.Type.ViewAction, action);

            }

            receivers.put(actorName, actor);

            if (actors == null) {
                actors = new ArrayList<>();
                this.childInstances.put(actorName, actors);
            }

            actors.add(actor);
        }

        return Behaviors.same();
    }

    protected Behavior<KlabMessage> handleFireMessage(Fire message) {

        if (message.appId != null) {
            ActorRef<KlabMessage> receiver = receivers.get(message.appId);
            if (receiver != null) {
                receiver.tell(message.direct());
            }
        } else {
            if (message.listenerId != null) {
                MatchActions actions = listeners.get(message.listenerId);
                if (actions != null) {
                    actions.match(message.value, message.scopeVars);
                    // if (message.finalize) {
                    // listeners.remove(message.listenerId);
                    // }
                }
            }

            if (message.semaphore != null) {
                Actors.INSTANCE.expire(message.semaphore);
            }
        }
        return Behaviors.same();
    }

    protected Behavior<KlabMessage> handleLoadBehaviorMessage(Load message) {

        this.parentActor = message.parent;
        message.scope.globalSymbols = this.symbolTable;
        IBehavior behavior = Actors.INSTANCE.getBehavior(message.behavior);
        if (behavior == null) {
            message.scope.runtimeScope.getMonitor().error("can't load unknown behavior " + message.behavior);
            return Behaviors.ignore();
        }

        if (message.forwardApplicationId != null) {

            /*
             * spawn another actor and load the behavior in it.
             */
            ActorRef<KlabMessage> child = getContext()
                    .spawn(SessionActor.create((Session) identity, message.forwardApplicationId), message.forwardApplicationId);
            this.receivers.put(message.forwardApplicationId, child);
            child.tell(message.direct());

        } else {
            /*
             * start running anything that starts automatically in a thread and exit, otherwise we
             * won't be able to run any other message to this actor until execution has finished.
             */
            runBehavior(message);
        }

        return Behaviors.same();
    }

    private void runBehavior(final Load message) {

        new Thread(){

            @Override
            public void run() {

                try {

                    boolean rootView = message.scope.viewScope == null ? false : (message.scope.viewScope.layout == null);

                    /*
                     * preload system actors. We don't add "self" which should be factored out by
                     * the interpreter.
                     */
                    if (!receivers.containsKey("user")) {
                        ActorRef<KlabMessage> sact = null;
                        ActorRef<KlabMessage> eact = null;
                        if ((ActorReference) identity.getParentIdentity(Session.class).getActor() != null) {
                            sact = ((ActorReference) identity.getParentIdentity(Session.class).getActor()).actor;
                        }
                        if (identity.getParentIdentity(EngineUser.class).getActor() != null) {
                            eact = ((ActorReference) identity.getParentIdentity(EngineUser.class).getActor()).actor;
                        }
                        // these three are the same. TODO check
                        receivers.put("session", sact);
                        receivers.put("view", sact);
                        receivers.put("system", sact);
                        // user actor
                        receivers.put("user", eact);
                        // TODO modeler actor - which can create and modify projects and code
                    }

                    KlabActor.this.behavior = Actors.INSTANCE.getBehavior(message.behavior);
                    KlabActor.this.listeners.clear();
                    KlabActor.this.actionBindings.clear();
                    KlabActor.this.actionCache.clear();
                    KlabActor.this.childActorPath = message.childActorPath;

                    /*
                     * load all imported and default libraries
                     */
                    KlabActor.this.libraries.putAll(Actors.INSTANCE.getLibraries(KlabActor.this.behavior.getStatement(),
                            message.scope.runtimeScope.getMonitor()));

                    for (Library library : KlabActor.this.libraries.values()) {
                        if (library.cls != null) {
                            KlabActor.this.nativeLibraryInstances.put(library.name,
                                    library.cls.getDeclaredConstructor().newInstance());
                        }
                    }

                    if (message.applicationId != null) {
                        // this only happens when we're spawning a component from a top application
                        // using new; in that case, the appId is communicated here and the appId in
                        // the message (which does not come from an application action) is null.
                        // This
                        // ensures that all component actors have the same appId.
                        KlabActor.this.appId = message.applicationId;
                    }

                    /*
                     * Init action called no matter what and before the behavior is set; the onLoad
                     * callback intervenes afterwards. Do not create UI (use raw scope).
                     */
                    for (IBehavior.Action action : KlabActor.this.behavior.getActions("init", "@init")) {
                        Scope initScope = message.scope.forInit();
                        initScope.metadata = new Parameters<>(message.metadata);
                        if (behavior.getDestination() == Type.SCRIPT || behavior.getDestination() == Type.UNITTEST) {
                            initScope = initScope.synchronous();
                        }
                        KlabActor.this.run(action, initScope);
                    }

                    /*
                     * run any main actions. This is the only action that may create a UI.
                     */
                    for (IBehavior.Action action : KlabActor.this.behavior.getActions("main", "@main")) {
                        Scope scope = message.scope.getChild(KlabActor.this.appId, action);
                        KlabActor.this.layout = scope.viewScope == null ? null : scope.viewScope.layout;
                        scope.metadata = new Parameters<>(message.metadata);
                        if (behavior.getDestination() == Type.SCRIPT || behavior.getDestination() == Type.UNITTEST) {
                            scope = scope.synchronous();
                        }
                        if (message.arguments != null) {
                            scope.symbolTable.putAll(message.arguments);
                        }
                        KlabActor.this.run(action, scope);
                    }

                    if (KlabActor.this.behavior.getDestination() == Type.UNITTEST) {
                        for (IBehavior.Action action : KlabActor.this.behavior.getActions("@test")) {
                            Scope testScope = message.scope.forTest(action);
                            testScope.metadata = new Parameters<>(message.metadata);
                            testScope.runtimeScope.getMonitor()
                                    .info(KlabActor.this.behavior.getName() + ": running test " + action.getName());
                            KlabActor.this.run(action, testScope);
                            testScope.testScope.finalizeTest(action, testScope.valueScope);
                        }
                        message.scope.runtimeScope.getMonitor().info(KlabActor.this.behavior.getName() + ": done running tests");
                    }

                    /*
                     * send the view AFTER running main and collecting all components that generate
                     * views.
                     */
                    if (rootView && message.scope.viewScope.layout != null) {
                        if (Configuration.INSTANCE.isEchoEnabled()) {
                            System.out.println(Actors.INSTANCE.dumpView(message.scope.viewScope.layout));
                        }
                        KlabActor.this.identity.setView(new ViewImpl(message.scope.viewScope.layout));
                        KlabActor.this.identity.getMonitor().send(IMessage.MessageClass.UserInterface,
                                IMessage.Type.SetupInterface, message.scope.viewScope.layout);
                    } /*
                       * TODO else if we have been spawned by a new component inside a group, we
                       * should send the group update message
                       */
                    /*
                     * move on, you waiters
                     */
                    for (Semaphore semaphore : message.getSemaphores(Semaphore.Type.LOAD)) {
                        Actors.INSTANCE.expire(semaphore);
                    }

                } catch (Throwable e) {

                    message.scope.onException(e, null);

                } finally {

                    if (message.scope.testScope != null) {
                        message.scope.testScope.finalizeTestRun();
                    }

                    if (message.scope.identity instanceof Session) {
                        if (KlabActor.this.appId != null && (KlabActor.this.behavior.getDestination() == Type.SCRIPT
                                || KlabActor.this.behavior.getDestination() == Type.UNITTEST)) {
                            /*
                             * communicate end of script to session
                             */
                            ((Session) message.scope.identity).notifyScriptEnd(KlabActor.this.appId);
                        }
                    }
                }

            }

        }.start();
    }

    /**
     * Execute action through actor message. Either references one of our k.Actors action or has an
     * appId to forward to. Unknown actions are sent to either a specialized method (TODO) or to the
     * user actor with a specific message.
     * 
     * @param message
     * @return
     */
    protected Behavior<KlabMessage> handleCallMessage(KActorsMessage message) {

        /**
         * Route only those messages whose appID is recognized, meaning they are directed through us
         * to one of our app executors. Others with appId will come from an application but our
         * agent doesn't have its own behavior loaded, so continue assuming it's for us.
         */
        if (message.appId != null && receivers.containsKey(message.appId)) {
            ActorRef<KlabMessage> receiver = receivers.get(message.appId);
            if (receiver != null) {
                receiver.tell(message.direct());
            }
        } else {

            /*
             * it's for us: our appId should be in the scope.
             */
            Action action = this.behavior == null ? null : this.behavior.getAction(message.message);
            if (action != null) {
                run(action, message.scope.withSender(message.sender, this.appId));
            } else {
                /*
                 * unknown: send to the user actor which may implement this for logging or
                 * redirection. TODO bind to an action to intercept unknown messages if defined in
                 * our own behavior first.
                 */
                this.identity.getParentIdentity(EngineUser.class).getActor().tell(new UnknownMessage(message, null));
            }
        }

        return Behaviors.same();
    }

    /**
     * Set the appropriate actor in the identity. Asking end may wait until that is done but we do
     * not reply otherwise.
     * 
     * @param message
     * @return
     */
    protected Behavior<KlabMessage> handleCreateChildMessage(Spawn message) {

        if (message.appId != null) {
            ActorRef<KlabMessage> receiver = receivers.get(message.appId);
            if (receiver != null) {
                receiver.tell(message.direct());
            }
        } else {

            Behavior<KlabMessage> behavior = null;
            // TODO potentially more differentiation according to host
            if (message.identity instanceof Observation) {
                behavior = ObservationActor.create((Observation) message.identity, message.appId);
            } else if (message.identity instanceof Session) {
                behavior = SessionActor.create((Session) message.identity, message.appId);
            }
            ActorRef<KlabMessage> actor = getContext().spawn(
                    Behaviors.supervise(behavior).onFailure(SupervisorStrategy.resume().withLoggingEnabled(true)),
                    message.identity.getId());
            message.identity.instrument(new ActorReference(actor));
        }

        return Behaviors.same();
    }

    @Override
    public String toString() {
        return "{" + getContext().getSelf() + " - " + behavior.getName() + "}";
    }

}
