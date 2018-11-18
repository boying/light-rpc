import com.netflix.hystrix.*;

/**
 * Created by boying on 2018/11/9.
 */
public class HystrixTest {
    public static class OrderServiceProvider {
        public Integer queryByOrderId() {
            System.out.println("queryByOrderId " + Thread.currentThread().getName());
            if (true) {
                throw new RuntimeException("fsfsxxxx");
            }
            return 1;
        }
    }

    public static class QueryOrderIdCommand extends HystrixCommand<Integer> {
        private OrderServiceProvider orderServiceProvider;
        private RuntimeException exception;

        // https://my.oschina.net/7001/blog/1619842
        public QueryOrderIdCommand(OrderServiceProvider orderServiceProvider) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("orderService"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("queryByOrderId"))
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                            .withFallbackEnabled(true)
                            .withCircuitBreakerRequestVolumeThreshold(10)////至少有10个请求，熔断器才进行错误率的计算
                            .withCircuitBreakerSleepWindowInMilliseconds(5000)//熔断器中断请求5秒后会进入半打开状态,放部分流量过去重试
                            .withCircuitBreakerErrorThresholdPercentage(50)//错误率达到50开启熔断保护
                            .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                            .withExecutionIsolationSemaphoreMaxConcurrentRequests(10)));
            this.orderServiceProvider = orderServiceProvider;
        }

        @Override
        protected Integer run() {
            try {
                return orderServiceProvider.queryByOrderId();
            }
            catch (RuntimeException e){
                this.exception = e;
                throw e;
            }
        }

        @Override
        protected Integer getFallback() {
            System.out.println("in fallback: " + getFailedExecutionException());
            return 1;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("main " + Thread.currentThread().getName());
        OrderServiceProvider orderServiceProvider = new OrderServiceProvider();
        for (int i = 0; i < 300; ++i) {
            QueryOrderIdCommand queryOrderIdCommand = new QueryOrderIdCommand(orderServiceProvider);
            try {
                Integer execute = queryOrderIdCommand.execute();
                System.out.println(execute);
            }catch (Exception e){
                System.out.println(e.getClass().getName());
            }
            Thread.sleep(300);
        }


    }
}
