package uz.darkselect.user_service_task.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class AbstractCacheWarmupTest {
    private TestCacheWarmup cacheWarmup;

    @BeforeEach
    void setUp() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.initialize();
        cacheWarmup = new TestCacheWarmup(executor);
    }

    @Test
    void init_whenEnabled_triggersWarmup() throws Exception {
        ReflectionTestUtils.setField(cacheWarmup, "warmupEnabled", true);
        ReflectionTestUtils.setField(cacheWarmup, "warmupTimeout", 1000);
        ReflectionTestUtils.setField(cacheWarmup, "batchSize", 10);

        AtomicBoolean called = new AtomicBoolean(false);
        cacheWarmup.onWarmupStart = () -> called.set(true);

        cacheWarmup.init();
        Thread.sleep(50);

        assertTrue(called.get());
    }

    @Test
    void init_whenDisabled_doesNotTriggerWarmup() throws Exception {
        ReflectionTestUtils.setField(cacheWarmup, "warmupEnabled", false);
        ReflectionTestUtils.setField(cacheWarmup, "batchSize", 10);

        AtomicBoolean called = new AtomicBoolean(false);
        cacheWarmup.onWarmupStart = () -> called.set(true);

        cacheWarmup.init();
        Thread.sleep(50);

        assertFalse(called.get());
    }

    @Test
    void warmUpCache_batchesAndStops() {
        ReflectionTestUtils.setField(cacheWarmup, "batchSize", 2);
        List<User> users = List.of(
                new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "A"),
                new User(UUID.fromString("00000000-0000-0000-0000-000000000002"), "B"),
                new User(UUID.fromString("00000000-0000-0000-0000-000000000003"), "C")
        );
        cacheWarmup.setUsers(users);
        cacheWarmup.warmUpCache();
        assertEquals(3, cacheWarmup.getCached().size());
    }

    @Test
    void warmUpCache_empty_noCaching() {
        ReflectionTestUtils.setField(cacheWarmup, "batchSize", 2);
        cacheWarmup.setUsers(List.of());
        cacheWarmup.warmUpCache();
        assertTrue(cacheWarmup.getCached().isEmpty());
    }

    @Test
    void warmUpCache_fetchThrows_retriesThenAbort() {
        ReflectionTestUtils.setField(cacheWarmup, "batchSize", 1);
        cacheWarmup.setFetchException(new RuntimeException("fail"));
        cacheWarmup.warmUpCache();
        assertTrue(cacheWarmup.getCached().isEmpty());
    }

    static class User {
        private final UUID id;
        User(UUID id, String name) { this.id = id; }
        UUID getId() { return id; }
    }

    static class Entity {
        private final UUID id;
        Entity(UUID id) { this.id = id; }
    }

    static class TestCacheWarmup extends AbstractCacheWarmup<User, Entity> {
        List<User> users = List.of();
        List<Entity> cached = new java.util.ArrayList<>();
        RuntimeException ex;
        Runnable onWarmupStart;

        TestCacheWarmup(ThreadPoolTaskExecutor exec) { super(exec); }

        void setUsers(List<User> list) { this.users = list; }
        List<Entity> getCached() { return cached; }
        void setFetchException(RuntimeException ex) { this.ex = ex; }

        @Override
        protected void warmUpCache() {
            if (onWarmupStart != null) onWarmupStart.run();
            super.warmUpCache();
        }

        @Override
        protected List<User> fetchBatch(UUID lastId, int batchSize) {
            if (ex != null) throw ex;
            return users.stream()
                    .filter(u -> u.getId().compareTo(lastId) > 0)
                    .limit(batchSize)
                    .toList();
        }

        @Override
        protected UUID getLastId(List<User> entities) {
            return entities.isEmpty() ? new UUID(0,0) : entities.get(entities.size() - 1).getId();
        }

        @Override
        protected Entity mapToCacheEntity(User entity) {
            return new Entity(entity.getId());
        }

        @Override
        protected void saveCache(List<Entity> cacheEntities) {
            cached.addAll(cacheEntities);
        }

        @Override
        protected String getCacheName() {
            return "test-cache";
        }
    }
}