package org.integratedmodelling.klab.components.runtime.actors;

import org.integratedmodelling.kactors.model.KActorsValue;
import org.integratedmodelling.kim.api.IKimExpression;
import org.integratedmodelling.kim.api.IParameters;
import org.integratedmodelling.klab.Actors;
import org.integratedmodelling.klab.Klab;
import org.integratedmodelling.klab.Urn;
import org.integratedmodelling.klab.api.data.general.IExpression.CompilerOption;
import org.integratedmodelling.klab.api.extensions.actors.Action;
import org.integratedmodelling.klab.api.extensions.actors.Behavior;
import org.integratedmodelling.klab.api.runtime.monitoring.IMonitor;
import org.integratedmodelling.klab.components.runtime.actors.KlabActor.KlabMessage;
import org.integratedmodelling.klab.components.runtime.actors.KlabActor.KlabMessage.Semaphore;
import org.integratedmodelling.klab.components.runtime.actors.KlabActor.Scope;
import org.integratedmodelling.klab.components.runtime.actors.SystemBehavior.Fire;
import org.integratedmodelling.klab.engine.runtime.Session;
import org.integratedmodelling.klab.engine.runtime.api.IActorIdentity;
import org.integratedmodelling.klab.engine.runtime.code.ObjectExpression;
import org.integratedmodelling.klab.exceptions.KlabException;
import org.integratedmodelling.klab.rest.ViewComponent;
import org.integratedmodelling.klab.utils.Parameters;
import org.integratedmodelling.klab.utils.Utils;

import akka.actor.typed.ActorRef;

/**
 * Abstract class for system actions written in Java and defined within behavior classes. System
 * will store these based {@link Behavior} and {@link Action} annotations. All must have the same
 * constructor.
 * 
 * @author Ferd
 *
 */
public abstract class KlabActionExecutor {

    /**
     * An action implementing this interface will be saved in the actor where the calling behavior
     * is running and be enabled to receive later messages just like an actor.
     * 
     * @author Ferd
     *
     */
    public interface Actor {

        /**
         * The actor name, normally established using a tag.
         * 
         * @return
         */
        String getName();

        /**
         * Done by the calling actor using arguments and/or metadata
         * 
         * @param name
         */
        void setName(String name);

        /**
         * Implement the response to a messages sent in k.Actors.
         * 
         * @param message
         * @param scope
         */
        void onMessage(KlabMessage message, Scope scope);

    }

    /**
     * A component is an Actor that reacts through an MVC pattern.
     * 
     * @author Ferd
     *
     */
    public interface Component extends Actor {

        /**
         * Return a descriptor of the view component that will provide the view for this actor.
         * 
         * @return
         */
        public ViewComponent getViewComponent();

    }

    protected ActorRef<KlabMessage> sender;
    protected IParameters<String> arguments;
    // @Deprecated // REMOVE! just leave the appId and the monitor, take the rest by passing the
    // scope in context
    // protected KlabActor.Scope scope;
    protected IActorIdentity<KlabMessage> identity;
    protected Session session;
    // the ID of the call that generated this action in the k.Actors code. May be
    // null when the action is create by the
    // scheduler or other API.
    protected String callId;
    protected IMonitor monitor;
    protected String appId;
    ObjectExpression expression = null;

    protected final Boolean DEFAULT_FIRE = Boolean.TRUE;

    public KlabActionExecutor(IActorIdentity<KlabMessage> identity, IParameters<String> arguments, KlabActor.Scope scope,
            ActorRef<KlabMessage> sender, String callId) {
        this.sender = sender;
        this.session = identity == null ? null : identity.getParentIdentity(Session.class);
        this.arguments = arguments;
        // this.scope = scope;
        this.appId = scope.appId;
        this.monitor = scope.runtimeScope == null ? Klab.INSTANCE.getRootMonitor() : scope.runtimeScope.getMonitor();
        this.identity = identity;
        this.callId = callId;
    }

    /**
     * Called at actor shutdown
     */
    protected void dispose() {

    }

    public void fire(Object value, boolean isFinal, Scope scope) {
        if (scope.listenerId != null) {
            this.sender.tell(new Fire(scope.listenerId, value, isFinal, scope.appId, scope.semaphore));
        }
    }

    public void fail(Scope scope, Object... args) {
        if (args != null) {
            monitor.error(args);
        }
        fire(false, true, scope);
    }

    protected Object evaluateArgument(String argument, Scope scope) {
        Object arg = arguments.get(argument);
        if (arg instanceof KActorsValue) {
            arg = evaluateInContext((KActorsValue) arg, scope);
        }
        return arg;
    }

    protected void error(String message) {
        // TODO actor-specific error management
        monitor.error(message);
    }

    protected Object evaluateInContext(KActorsValue arg, Scope scope) {
        switch(arg.getType()) {
        case ANYTHING:
        case ANYVALUE:
        case EMPTY:
            break;
        case OBJECT:
            return Actors.INSTANCE.createJavaObject(arg.getConstructor(), scope, identity);
        case ANYTRUE:
            return true;
        case ERROR:
            throw arg.getValue() instanceof Throwable
                    ? new KlabException((Throwable) arg.getValue())
                    : new KlabException(
                            arg.getValue() == null ? "Unspecified actor error from error value" : arg.getValue().toString());

        case NUMBERED_PATTERN:
        case IDENTIFIER:
            return scope.getValue(arg.getValue().toString());

        case EXPRESSION:

            if (this.expression == null) {
                this.expression = new ObjectExpression((IKimExpression) arg.getValue(), scope.runtimeScope,
                        CompilerOption.WrapParameters);
            }
            return this.expression.eval(scope.runtimeScope, identity, Parameters.create(scope.symbolTable));

        case BOOLEAN:
        case CLASS:
        case DATE:
        case NUMBER:
        case RANGE:
        case STRING:
        case OBSERVABLE:
        case QUANTITY:
            return arg.getValue();
        case OBSERVATION:
            // TODO
            break;
        case SET:
            // eval all args
            break;
        case LIST:
            // eval all args
            break;
        case TREE:
            // eval all args
            break;
        case MAP:
            break;
        case NODATA:
            return null;
        // return Observables.INSTANCE.declare(arg.getValue().toString());
        case REGEXP:
            break;
        case TABLE:
            break;
        case TYPE:
            break;
        case URN:
            return new Urn(arg.getValue().toString());
        // default:
        // break;
        case CONSTANT:
            break;
        }
        return arg.getValue();
    }

    protected <T> T evaluateArgument(String argument, Scope scope, T defaultValue) {
        Object arg = evaluateArgument(argument, scope);
        return arg == null ? defaultValue : Utils.asType(arg, defaultValue == null ? Object.class : defaultValue.getClass());
    }

    protected <T> T evaluateArgument(int argumentIndex, Scope scope, T defaultValue) {
        Object arg = evaluateArgument(argumentIndex, scope);
        return arg == null ? defaultValue : Utils.asType(arg, defaultValue == null ? Object.class : defaultValue.getClass());
    }

    protected Object evaluateArgument(int argumentIndex, Scope scope) {
        Object arg = null;
        if (arguments != null && arguments.getUnnamedKeys().size() > argumentIndex) {
            arg = arguments.get(arguments.getUnnamedKeys().get(argumentIndex));
            if (arg instanceof KActorsValue) {
                arg = evaluateInContext((KActorsValue) arg, scope);
            }
        }
        return arg;
    }

    public String getStatementId() {
        return callId;
    }

    /**
     * May be called more than once, so pass the scope again.
     * 
     * @param scope
     */
    abstract void run(KlabActor.Scope scope);

}
