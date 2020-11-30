package kfk;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import decoder.Record;

public class RecordDeseralizer implements Deserializer<Record>{
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // TODO Auto-generated method stub
 
    }
    @Override
    public Record deserialize(String topic, byte[] bytes) {
         return (Record)SerializeUtil.deserialize(bytes,Record.class);
    }
    @Override
    public void close() {
        // TODO Auto-generated method stub
 
    }
}
