package decoder;

public class AppConfig {
	public void print() {
		System.out.println(dataSource);
		System.out.println(RECPath);
		System.out.println(FTPHost);
		System.out.println(FTPPort);
		System.out.println(FTPUser);
		System.out.println(FTPPassword);
		System.out.println(HBaseConfig);
		System.out.println(KafkaConfig);
		System.out.println(RECConfPath);
		System.out.println(dataConfPath);
		System.out.println(outPath);
	}
	public void setRECPath(String RecPath) {
		RECPath = RecPath;
	}
	public void setRECConfPath(String RECConfPath) {
		this.RECConfPath = RECConfPath;
	}
	public void setDataConfPath(String dataConfPath) {
		this.dataConfPath = dataConfPath;
	}
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	public void setFTPHost(String fTPHost) {
		FTPHost = fTPHost;
	}
	public void setFTPPort(String fTPPort) {
		FTPPort = fTPPort;
	}
	public void setFTPUser(String fTPUser) {
		FTPUser = fTPUser;
	}
	public void setFTPPassword(String fTPPassword) {
		FTPPassword = fTPPassword;
	}
	public void setFTPRECPath(String fTPRECPath) {
		FTPRECPath = fTPRECPath;
	}
	public void setHBaseConfig(String hBaseConfig) {
		HBaseConfig = hBaseConfig;
	}
	public void setKafkaConfig(String kafkaConfig) {
		KafkaConfig = kafkaConfig;
	}
	public void setFTPMode(String fTPMode) {
		FTPMode = fTPMode;
	}
	public void setFTPTransInterval(String fTPTransInterval) {
		FTPTransInterval = fTPTransInterval;
	}
	public void setTableName(String tableName) {
		TableName = tableName;
	}
	public void setTopicName(String topicName) {
		TopicName = topicName;
	}
	public String getRECConfPath() {
		return RECConfPath;
	}
	public String getDataConfPath() {
		return dataConfPath;
	}
	public String getRECPath() {
		return RECPath;
	}
	public String getOutPath() {
		return outPath;
	}
	public String getDataSource() {
		return dataSource;
	}
	public String getFTPHost() {
		return FTPHost;
	}
	public String getFTPPort() {
		return FTPPort;
	}
	public String getFTPUser() {
		return FTPUser;
	}
	public String getFTPPassword() {
		return FTPPassword;
	}
	public String getFTPRECPath() {
		return FTPRECPath;
	}
	public String getHBaseConfig() {
		return HBaseConfig;
	}
	public String getKafkaConfig() {
		return KafkaConfig;
	}
	public String getFTPMode() {
		return FTPMode;
	}
	public String getFTPTransInterval() {
		return FTPTransInterval;
	}
	public String getTableName() {
		return TableName;
	}
	public String getTopicName() {
		return TopicName;
	}
	private String dataSource;
	private String RECPath;
	private String FTPHost;
	private String FTPPort;
	private String FTPUser;
	private String FTPPassword;
	private String FTPMode;
	private String FTPRECPath;
	private String FTPTransInterval;
	
	private String HBaseConfig;
	private String TableName;
	private String KafkaConfig;
	private String TopicName;
	
	private String RECConfPath;
	private String dataConfPath;
	private String outPath;
	
	
}
