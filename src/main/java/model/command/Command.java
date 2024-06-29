/*
 *@Type Command.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 01:50
 * @version
 */
package model.command;

import dto.ActionDTO;
import dto.RespDTO;
import dto.RespStatusTypeEnum;
import service.Store;

import java.io.IOException;
import java.io.ObjectOutputStream;

public interface Command {
    String getKey();
    void execute(ActionDTO dto, ObjectOutputStream oos) throws IOException;
}

