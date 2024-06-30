package controller.factory;

import dto.ActionDTO;
import dto.RespDTO;
import service.Store;

public interface CommandHandler {
    RespDTO handle(ActionDTO dto, Store store);
}
