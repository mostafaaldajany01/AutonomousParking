package automaticparking;

public class FakeSensor implements Sensor{
	private int[] values;
	private int i = 0;
	
	@Override
	public int read() { 
		if (i >= values.length) i = 0;
		return values[i++];
	}
	
	public void setScript(int[] values)
	{
		this.values = values;
	}
	
	public FakeSensor(int[] values)
	{
		this.values = values;
	}
}
