

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Mapper;

public class InvertedIndexMapper extends Mapper<LongWritable, Text, Text, Text>{
	
	List<String> stopWords = new ArrayList<String>();
	
	//Keep a list of stop words
	@Override
	protected void setup(Context context) throws IOException {
		
		Configuration conf = context.getConfiguration();
		String filePath = conf.get("filePath");
		
		Path pt=new Path("hdfs:" + filePath);//Location of file in HDFS
        FileSystem fs = FileSystem.get(new Configuration());//FileSystem hadoop class
        BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
        String line;
        line=br.readLine();
        while (line != null){
            stopWords.add(line.trim().toLowerCase());
            line=br.readLine();
        }
	}
	
	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		
		//https://hadoop.apache.org/docs/r2.4.1/api/org/apache/hadoop/mapred/FileSplit.html
		String fileName = ((FileSplit) context.getInputSplit()).getPath().getName();
		Text name = new Text(fileName);
		
		//fast & iterator don't have to load all data
		StringTokenizer tokenizor = new StringTokenizer(value.toString());
		while(tokenizor.hasMoreTokens()) {
			String curWord = tokenizor.nextToken().toString().toLowerCase();
			curWord = curWord.replaceAll("[^a-zA-Z]", "");
			//filter out the stop word
			if(!stopWords.contains(curWord))
				context.write(new Text(curWord), name);
		}
	}
}
