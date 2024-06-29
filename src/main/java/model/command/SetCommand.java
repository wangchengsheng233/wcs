/*
 *@Type SetCommand.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 01:59
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
public class SetCommand extends AbstractCommand {
    private String key;

    private String value;

    public SetCommand(String key, String value) {
        super(CommandTypeEnum.SET);
        this.key = key;
        this.value = value;
    }

    @Override
    public void execute(ActionDTO dto, ObjectOutputStream oos) throws IOException {

    }
}
