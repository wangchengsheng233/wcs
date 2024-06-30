/*
 *@Type ConvertUtil.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 02:09
 * @version
 */
package utils;

import com.alibaba.fastjson.JSONObject;
import model.command.Command;
import model.command.CommandTypeEnum;
import model.command.RmCommand;
import model.command.SetCommand;

/*
CommandUtil 类提供了一个静态方法 jsonToCommand，用于将 JSON 对象转换为相应的 Command 子类对象。
该方法根据 JSON 对象中的 type 字段确定命令的类型，并将其转换为 SetCommand 或 RmCommand 对象。如果类型不匹配，则返回 null。
通过这种方式，可以根据 JSON 对象的内容动态地创建不同类型的命令对象，提高了代码的灵活性和可扩展性
* */
public class CommandUtil {
    public static final String TYPE = "type";

    public static Command jsonToCommand(JSONObject value){
        if (value.getString(TYPE).equals(CommandTypeEnum.SET.name())) {
            return value.toJavaObject(SetCommand.class);
        } else if (value.getString(TYPE).equals(CommandTypeEnum.RM.name())) {
            return value.toJavaObject(RmCommand.class);
        }
        return null;
    }
}
