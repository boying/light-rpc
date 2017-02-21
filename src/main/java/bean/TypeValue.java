package bean;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
@Data
@RequiredArgsConstructor
public class TypeValue {
    private final String type;
    private final String value;
}
