package com.noraemoa.songrec.config.perf;

/**
 * 요청 1건(= 스레드 1개)이 DB 커넥션을 얼마나 오래 붙잡았는지 누적하는 측정기.
 *
 * start()~stop() 사이에 같은 스레드에서 일어난 커넥션 획득/반납을
 * ConnectionTrackingDataSourceConfig 의 DataSource 프록시가 여기에 기록한다.
 * - holdMs: 커넥션을 빌린 순간부터 반납(close)할 때까지 = 커넥션 점유 시간
 * - waitMs: 풀에서 커넥션을 받기까지 기다린 시간 = 풀 고갈 지표
 */
public final class ConnectionUsageTracker {

  private static final ThreadLocal<Stats> CURRENT = new ThreadLocal<>();

  private ConnectionUsageTracker() {
  }

  public static void start() {
    CURRENT.set(new Stats());
  }

  public static Stats stop() {
    Stats stats = CURRENT.get();
    CURRENT.remove();
    return stats == null ? new Stats() : stats;
  }

  static void recordWait(long waitNanos) {
    Stats stats = CURRENT.get();
    if (stats != null) {
      stats.count++;
      stats.waitNanos += waitNanos;
      stats.maxWaitNanos = Math.max(stats.maxWaitNanos, waitNanos);
    }
  }

  static void recordHold(long holdNanos) {
    Stats stats = CURRENT.get();
    if (stats != null) {
      stats.holdNanos += holdNanos;
      stats.maxHoldNanos = Math.max(stats.maxHoldNanos, holdNanos);
    }
  }

  public static class Stats {

    private int count;
    private long holdNanos;
    private long maxHoldNanos;
    private long waitNanos;
    private long maxWaitNanos;

    public int count() {
      return count;
    }

    public long holdTotalMs() {
      return holdNanos / 1_000_000;
    }

    public long holdMaxMs() {
      return maxHoldNanos / 1_000_000;
    }

    public long waitTotalMs() {
      return waitNanos / 1_000_000;
    }

    public long waitMaxMs() {
      return maxWaitNanos / 1_000_000;
    }
  }
}
