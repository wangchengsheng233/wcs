/*
 *@Type RmCommand.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 01:57
 * @version
 */
package model.command;

import dto.ActionDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectOutputStream;

@Setter
@Getter
public class RmCommand extends AbstractCommand {
    private String key;

    public RmCommand(String key) {
        super(CommandTypeEnum.RM);
        this.key = key;
    }

    @Override
    public void execute(ActionDTO dto, ObjectOutputStream oos) throws IOException {

    }
}
