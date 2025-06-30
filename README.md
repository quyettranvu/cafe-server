### Other features related to Kafka can be customized and configured more:

1. RecordFilterStrategy configured in factory to skip messages before processing.
2. Message conversion leverages (besides initial deserialization in producer/consumer), it can be customized more in factory.
3. Dead Letter Published in automatically forwarding failed messages to another topic.
4. Manual partition assignment for which listener/consumer. (use annotation @Partition).
5. Control behavior when Kafka rebalances consumers (define custom rebalancing listener).
6. Leverage secure communication over SSL or SASL (production), define trust store location.
7. Schema Management with Avro & Schema Registry to enforce data contracts: ensure backward/forward compatibility in microservice architecture and mutable schemas, binary use enhances higher efficiency and reduces costs when schema changes.
8. In case of saving all states of change log without using monitoring tools, ensure that compact policy must be set to default (delete).
9. Performance Optimization:
   - linger.ms: Kafka producer will wait for a specific interval before grouping messages and sending in a batch.
   - compression.type: define compression algorithm for messages before sending, saving bandwidth and disk storage. Example: Snappy compression algorithm.
10. Key concepts of Kafka Streaming:
    - KStream: represents a stream of records (like a topic of events).
    - KTable: a changelog stream of records (use a compacted topic to build a cached view of data).
    - GlobalKTable: similar to KTable but replicated across all instances.
    - Windowing: aggregate over time intervals (e.g., 5-minute windows for performing logic actions), define a state and distinguished result → when using with count() will return KTable, convert to KStream with toStream()
    - State store: local store to hold aggregation state. Kafka will create state store(RocksDB or in-memory store) to save key, value of KStream and make changelog on a hidden topic for preserving state.


### Batch Processing Strategies and Principles
1. Batch Processing là xử lý dữ liệu lớn theo từng lô (batch) thay vì xử lý từng bản ghi riêng lẻ, thường được dùng cho các công việc như:
    - Import/export dữ liệu (CSV, DB, XML,…)
    - Xử lý báo cáo định kỳ
    - Dọn dẹp dữ liệu
....

2. Nguyên tắc & chiến lược trong Spring Batch

    a. Cấu trúc chính của Spring Batch
   - Job: Một tiến trình xử lý batch tổng thể
   - Step: Mỗi bước trong Job (đọc, xử lý, ghi)
   - ItemReader: Đọc dữ liệu đầu vào
   - ItemProcessor: Xử lý/biến đổi dữ liệu
   - ItemWriter: Ghi dữ liệu sau xử lý

    b. Chiến lược xử lý hiệu quả
   - Chunk-based processing: Đọc, xử lý và ghi theo từng khối (chunk) – ví dụ: mỗi lần xử lý 100 bản ghi.
   - Fault tolerance: Cho phép bỏ qua lỗi (skip) hoặc retry lại khi lỗi tạm thời.
   - Parallel processing: Chạy nhiều Step hoặc Tasklet song song để tăng hiệu năng.
   - Partitioning: Chia nhỏ dữ liệu cho nhiều thread hoặc máy để xử lý song song.
   - Scheduling: Kết hợp với Spring Scheduler / Quartz để lên lịch tự động chạy job.
...
   3. Nguyên tắc thiết kế tốt
   - Tách riêng cấu hình Job/Step ra khỏi business logic
   - Sử dụng Stateless Processor (không giữ trạng thái)
   - Ghi log chi tiết để dễ theo dõi tiến trình
   - Quản lý trạng thái job qua JobRepository (Spring cung cấp sẵn)
   - Xử lý rollback chính xác khi ghi lỗi


### COMMANDS

1. MySQL (MySQL 8.0, default user: root)
   docker run --name mysql8 \
   -e MYSQL_ROOT_PASSWORD=MyRootPwd \
   -e MYSQL_DATABASE=cafequyettranvu \
   -e MYSQL_USER=quyettranvu \
   -e MYSQL_PASSWORD=********** \
   -p 3306:3306 -d mysql:8


2. Redis (Redis 7.2)
   docker run -d \
   --name redis-cafequyet \
   -p 6379:6379 \
   -e REDIS_PASSWORD=********** \
   redis:7.2 \
   redis-server --requirepass **********

3. Kafka: (sử dụng Docker và Zookeeper)
   docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper
   docker run -d --name kafka \
   -p 9092:9092 \
   -e KAFKA_ZOOKEEPER_CONNECT=host.docker.internal:2181 \
   -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
   -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
   confluentinc/cp-kafka

4. Spring Batch
   Về Meta-data schema quản lý bởi JobRepository:
   - spring.batch.jdbc.initialize-schema=always -> đặt trong application.properties để thêm các bảng BATCH_JOB_INSTANCE, BATCH_JOB_EXECUTION,...
   => Lấy được thông tin datasource, Spring sẽ thêm Spring Batch schemas vào trong DB (quản lý bởi JobRepository): quản lý những metadata về xử lý dữ liệu lớn, ghi log, trạng thái, khôi phục, tránh chạy trùng lặp,...
   - Việc sử dụng annotation EnableBatchProcessing đã cài mặc định JobRepository. Bản thân JobRepositry sẽ quản lý các job, step, ghi log, trạng thái, restart, executionId,...
   => có thể dùng in-memory JobRepository nhưng sẽ mất đi các tính năng ở trên, thường dùng trong test.
   Tham khảo thêm về mô hình các bảng: https://docs.spring.io/spring-batch/reference/schema-appendix.html

   Về JobRegistry và JobExplorer:
   - Khi cần chạy job theo tên (dynamic), lấy job từ jobName chứ không phải inject vào. (JobRegistry)
   - Cho phép tra cứu metadata của các Job(chỉ đọc, ghi là do JobRepository) (JobExplorer)

   Về hướng thiết kế trong Step:
   - Cấu hình Delegate Pattern ở ItemReader, ItemWriter, processor giúp tách biệt việc xử lý logic khỏi tổng thể.
   - Có thể sử dụng composite pattern để thực hiện những business logic thêm vào trước khi read/process/write: có thể hiểu là ItemWriter that contains another ItemWriter or an ItemReader that contains another ItemReader, đổi với processor chỉ đơn giản implements ItemProcessor nhận một kiểu và trả về kiểu khác.
   - Ngoài ra đối với processor có thể thực hiện một chain of item như biến đối, filter, validating,...
   Những common batch pattern có thể cấu hình thêm: https://docs.spring.io/spring-batch/reference/common-patterns.html
   - Có thể cấu hình listener để kiểm tra log và xử lý lỗi (khai báo class extends từ ItemListenerSupport)
   - Có thể gắn chính sách để kết thúc Step (implements StepListener -> sử dụng stepExecution để stop/terminate)
   
   Về Mapper khi read data:
   - Nếu đọc từ DB, sử dụgn RowMapper<T> với interface JdbcCursorItemReader và object truyền vào là ResultSet
   - Nếu đọc từ file CSV, TXT với interface FlatFileItemReader và object truyền vào là FieldSet

   Về Tasklet:
   - Một giao diện đại diện cho một tác vụ xử lý riêng lẻ, không chia theo chunk (không chia theo chunk để process)
   - Thường dùng để xử lý MỘT lần duy nhất

5. Spring Data Redis
   Về thuật ngữ chung: trong source đang xem consumer là subscriber theo docs của Spring Data Redis
   
   Về Cache Manager: RedisCacheManager chỉ hỗ trợ cache-level locking, còn nếu muốn custom per-key locking phải viết một cơ chế lấy dữ liệu qua key này (bỏ qua Cacheable)

   Về Object Mapping: Trong source đã sử dụng hai cách:
  - Convert to simple value using e. g. a String JSON representation.
  - Serialize the value with a suitable RedisSerializer.
  - Convert the value into a Map suitable for serialization using a HashMapper. (tham khảo thêm ở: https://docs.spring.io/spring-data/redis/reference/redis/redis-streams.html)

   Các nội dung có thể triển khai thêm:
   - Keyspaces: custom prefix cho key khi lưu bằng Redis (sử dụng @RedisHash)
   - Secondary Index: sử dụng chỉ số phụ để truy vấn nhanh hơn (sử dụng @Indexed, @GeoIndexed)
   - Time to live: quy định thời gian tồn tại cho objects lưu trong Redis, trong source config chung ở Redis Cache Manager. Có thể custom cụ thể ở một lớp trong @RedisHash, @TimeToLive(cho hoặc thuộc tính hoặc phương thức trong cùng 1 class), thông qua Keyspace settings.
   - Redis sẽ không hỗ trợ native sorting trực tiếp từ Redis, nếu cần Truy vấn tất cả các bản ghi phù hợp trước, sau đó sort trong bộ nhớ Java (bằng Comparator) trước khi gọi logic xử lý.
   - Query theo filter, matchers: https://docs.spring.io/spring-data/redis/reference/redis/redis-repositories/query-by-example.html
   - Class-based Projections (DTOs): hướng thiết kế truy vấn đối tượng trả về linh hoạt
   - phát sinh các sự kiện nghiệp vụ (domain events) một cách rõ ràng và tự động khi có thay đổi dữ liệu, mà không cần viết thủ công ở Service hay Listener. (lưu ý là thao tác với entity hoặc save hoặc delete thôi), sử dụng @DomainEvents (chẳng hạn để phát đi thông báo được publish sau khi đã lưu thành công). Ngoài ra có @AfterDomainEventPublication
   
   Về Redis Cluster:
   - cơ chế cho phép phân tán dữ liệu Redis trên nhiều node (máy chủ). Mỗi cluster sẽ có các master nodes quản và mỗi có >=1 replicas
   - khi cần mở rộng chỉ cần thêm node, Redis cluster sẽ tự cân bằng tải lại

   Về Redis Persistence:
   - Tham khảo thêm: https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/
   - save 300 10 # Sau 300 giây nếu có ít nhất 10 thay đổi (example of a line setting in redis.conf)

   Các Keywords/Kiểu Collections Types khi làm việc với Spring Data Redis: https://docs.spring.io/spring-data/redis/reference/repositories/query-keywords-reference.html, https://docs.spring.io/spring-data/redis/reference/repositories/query-return-types-reference.html
   - Thuật ngữ (Glossary): https://docs.spring.io/spring-batch/reference/glossary.html


