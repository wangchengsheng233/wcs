package controller.factory;

import dto.ActionTypeEnum;

public class CommandHandlerFactory {
    public static CommandHandler createHandler(ActionTypeEnum type) {
        switch (type) {
            case GET:
                return new GetCommandHandler();
            case SET:
                return new SetCommandHandler();
            case RM:
                return new RemoveCommandHandler();
            default:
                throw new IllegalArgumentException("Unsupported action type");
        }
    }
}
