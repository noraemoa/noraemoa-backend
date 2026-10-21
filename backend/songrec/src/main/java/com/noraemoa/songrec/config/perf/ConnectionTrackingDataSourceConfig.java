package com.noraemoa.songrec.config.perf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * 커넥션 점유/대기 시간 측정용 DataSource 래퍼. (perf.connection-tracking.enabled=true 일 때만)
 *
 * 트랜잭션 안의 커넥션뿐 아니라, 트랜잭션 없이 실행되는 리포지토리 조회(자동 커밋)가
 * 잠깐 빌리는 커넥션까지 모두 잡기 때문에 "요청 1건이 커넥션을 붙잡은 시간"을 그대로 측정한다.
 */
@Configuration
@ConditionalOnProperty(name = "perf.connection-tracking.enabled", havingValue = "true")
public class ConnectionTrackingDataSourceConfig {

  @Bean
  public static BeanPostProcessor connectionTrackingDataSourcePostProcessor() {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource dataSource && !(bean instanceof TrackingDataSource)) {
          return new TrackingDataSource(dataSource);
        }
        return bean;
      }
    };
  }

  static class TrackingDataSource extends DelegatingDataSource {

    TrackingDataSource(DataSource target) {
      super(target);
    }

    @Override
    public Connection getConnection() throws SQLException {
      long start = System.nanoTime();
      Connection connection;
      try {
        connection = super.getConnection();
      } catch (SQLException e) {
        // 풀이 고갈돼 connection-timeout 까지 기다리다 실패한 경우도 대기 시간으로 남긴다.
        ConnectionUsageTracker.recordWait(System.nanoTime() - start);
        throw e;
      }
      return track(connection, start);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
      long start = System.nanoTime();
      Connection connection = super.getConnection(username, password);
      return track(connection, start);
    }

    private Connection track(Connection target, long requestedAt) {
      long acquiredAt = System.nanoTime();
      ConnectionUsageTracker.recordWait(acquiredAt - requestedAt);

      boolean[] closed = {false};
      return (Connection) Proxy.newProxyInstance(
          Connection.class.getClassLoader(),
          new Class<?>[]{Connection.class},
          (proxy, method, args) -> {
            if ("close".equals(method.getName()) && !closed[0]) {
              closed[0] = true;
              ConnectionUsageTracker.recordHold(System.nanoTime() - acquiredAt);
            }
            try {
              return method.invoke(target, args);
            } catch (InvocationTargetException e) {
              throw e.getTargetException();
            }
          });
    }
  }
}
