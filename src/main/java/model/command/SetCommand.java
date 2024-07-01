/*
 *@Type SetCommand.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 01:59
 * @version
 */
package model.command;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SetCommand extends AbstractCommand {
    private String key;

    private String value;

    /*
    * 0 :未删除
    * 1：已删除
    * */
    private  int deleted;

    public SetCommand(String key, String value) {
        super(CommandTypeEnum.SET);
        this.key = key;
        this.value = value;
        this.deleted = 0;
    }
}
