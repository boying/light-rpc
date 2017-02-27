package client;

import bean.Request;
import bean.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server_provider.IServerProvider;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
public class Task implements Callable<Result> {
    private static Logger logger = LoggerFactory.getLogger(Task.class);

    private IServerProvider hostProvider;
    private Method method;
    private Object[] args;

    public Task(IServerProvider hostProvider, Method method, Object[] args) {
        this.hostProvider = hostProvider;
        this.method = method;
        this.args = args;
    }

    @Override
    public Result call() throws Exception {
        Request request = RequestFactory.newRequest(method, args, false, 0);
        String body = Request2JsonSerializer.serialize(request);
        logger.debug("request is {}", body);
        String rsp = RequestJsonSender.send(hostProvider, body);
        logger.debug("response is {}", rsp);
        return Json2ResultDeserializer.deserialize(rsp, method.getReturnType());
    }

}
