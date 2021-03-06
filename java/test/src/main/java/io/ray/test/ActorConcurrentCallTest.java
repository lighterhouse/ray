package io.ray.test;

import com.google.common.collect.ImmutableList;
import io.ray.api.ActorHandle;
import io.ray.api.ObjectRef;
import io.ray.api.Ray;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class ActorConcurrentCallTest extends BaseTest {

  public static class ConcurrentActor {
    private final CountDownLatch countDownLatch = new CountDownLatch(3);

    public String countDown() {
      countDownLatch.countDown();
      try {
        countDownLatch.await();
        return "ok";
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void testConcurrentCall() {
    TestUtils.skipTestUnderSingleProcess();

    ActorHandle<ConcurrentActor> actor =
        Ray.actor(ConcurrentActor::new)
            .setMaxConcurrency(3)
            .remote();
    ObjectRef<String> obj1 = actor.task(ConcurrentActor::countDown).remote();
    ObjectRef<String> obj2 = actor.task(ConcurrentActor::countDown).remote();
    ObjectRef<String> obj3 = actor.task(ConcurrentActor::countDown).remote();

    List<Integer> expectedResult = ImmutableList.of(1, 2, 3);
    Assert.assertEquals(obj1.get(), "ok");
    Assert.assertEquals(obj2.get(), "ok");
    Assert.assertEquals(obj3.get(), "ok");
  }

}
