package kfk;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;

import app.App;
import decoder.Record;

public class RECProducer {
	private KafkaProducer<String, Record> producer=null;	//producer
	AdminClient client=null;	//adminclient
	
	private Properties kafkaProps;//producer配置
	
	private String topic;	//topic名
	private int numOfNodes = 0;//kafka集群节点数
	
	public RECProducer() {
		System.out.println("--- Initialize kafka producer ---");
		//配置
		kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, App.getApp().getAppConfig().getKafkaConfig());
		//序列化器
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "kfk.RecordSeralizer");

	}
	public void connect(String topic)
	{
		System.out.println("--- Producer connect to topic "+topic+" ---");
		this.topic = topic;
		client = AdminClient.create(kafkaProps);
		try {
			DescribeClusterResult ret = client.describeCluster();
			numOfNodes = ret.nodes().get().size();
			ListTopicsResult topics = client.listTopics(new ListTopicsOptions());
	        Set<String> topicNames = topics.names().get();
	        //没有这个topic就创建topic
	        if(!topicNames.contains(topic)) {
	        	createTopics(client);
	        	//TODO log
	        	System.out.println("Create topic: "+topic);
	        }
	        //创建生产者
	        producer = new KafkaProducer<>(kafkaProps);

		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println(e.toString());
		}
	}
	public void sendMessage(String key, Record value) {
		producer.send(new ProducerRecord<String, Record>(topic, key, value));
	}
	public void close() {
		System.out.println("--- Close kafka producer---");
		//关闭生产者
		if(producer!=null) {
			producer.close();
			producer = null;
		}
		//关闭adminclient
		if(client!=null) {
			client.close();
			client=null;
		}
	}
	public void printTopicInfo() {
		
        try {
        	System.out.println("AdminClientTest()");
        	describeCluster(client);
        	listAllTopics(client);
        	
//        	describeTopics(client);
//        	describeConfig(client);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println(e.toString());
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println(e.toString());
		}
	}
	/**
     * describe the cluster
     * @param client
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void describeCluster(AdminClient client) throws InterruptedException, ExecutionException  {
        DescribeClusterResult ret = client.describeCluster();
        System.out.println(String.format("Cluster id: %s, controller: %s", ret.clusterId().get(), ret.controller().get()));
        System.out.println("Current cluster nodes info: ");
        for (Node node : ret.nodes().get()) {
            System.out.println(node);
        }
    }
 
    /**
     * describe topic's config
     * @param client
     */
    private void describeConfig(AdminClient client) throws ExecutionException, InterruptedException {
        DescribeConfigsResult ret = client.describeConfigs(Collections.singleton(new ConfigResource(ConfigResource.Type.TOPIC, topic)));
        Map<ConfigResource, Config> configs = ret.all().get();
        for (Map.Entry<ConfigResource, Config> entry : configs.entrySet()) {
            ConfigResource key = entry.getKey();
            Config value = entry.getValue();
            System.out.println(String.format("Resource type: %s, resource name: %s", key.type(), key.name()));
            Collection<ConfigEntry> configEntries = value.entries();
            for (ConfigEntry each : configEntries) {
                System.out.println(each.name() + " = " + each.value());
            }
        }
 
    }
 
 
    /**
     * delete the given topics
     * @param client
     */
    private void deleteTopics(AdminClient client) throws ExecutionException, InterruptedException {
        KafkaFuture<Void> futures = client.deleteTopics(Arrays.asList(topic)).all();
        futures.get();
    }
 
    /**
     * describe the given topics
     * @param client
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void describeTopics(AdminClient client) throws ExecutionException, InterruptedException {
        DescribeTopicsResult ret = client.describeTopics(Arrays.asList(topic, "__consumer_offsets"));
        Map<String, TopicDescription> topics = ret.all().get();
        for (Map.Entry<String, TopicDescription> entry : topics.entrySet()) {
            System.out.println(entry.getKey() + " ===> " + entry.getValue());
        }
    }
 
    /**
     * create multiple sample topics
     * @param client
     */
    private void createTopics(AdminClient client) throws ExecutionException, InterruptedException {
        //topic名称，分区数，副本数。副本数不能超过broker数量
    	NewTopic newTopic = new NewTopic(topic, numOfNodes, (short)numOfNodes);
        CreateTopicsResult ret = client.createTopics(Arrays.asList(newTopic));
        ret.all().get();
    }
 
    /**
     * print all topics in the cluster
     * @param client
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void listAllTopics(AdminClient client) throws ExecutionException, InterruptedException {
        ListTopicsOptions options = new ListTopicsOptions();
        options.listInternal(true); // includes internal topics such as __consumer_offsets
        
        ListTopicsResult topics = client.listTopics(options);
        Set<String> topicNames = topics.names().get();
        System.out.println("Current topics in this cluster: " + topicNames);
        
    }

}
