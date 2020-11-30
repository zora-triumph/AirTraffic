package kfk;
import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import decoder.Record;

public class RecordSeralizer implements Serializer<Record>{
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // TODO Auto-generated method stub
 
    }
    @Override
    public byte[] serialize(String topic, Record data) {
         return SerializeUtil.serialize(data);
    }
    @Override
    public void close() {
        // TODO Auto-generated method stub
 
    }
	
}
