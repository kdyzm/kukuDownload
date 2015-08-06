package getBleachFromkukudm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * 从酷酷漫画网下载指定的一话死神漫画http://comic.kukudm.com/
 * 例：http://comic.kukudm.com/comiclist/6/47193/2.htm
 */
public class getAllComics {
	public static void main(String[] args) throws IOException {
		int pageSize=getPageSize();
		if(pageSize==0)
		{
			System.out.println("没找到相关匹配项目！");
			return;
		}
		List<String>list=getPageList(pageSize);
		List<String>urls=getAllUrlFromPerPage(list);
		writeToFile(urls);
		System.out.println("解析结果全部保存到了allBleachComics.txt文件中！");
	}
	//将结果保存到allBleachComics.txt文件中
	private static void writeToFile(List<String> urls) throws IOException {
		PrintWriter pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream("allBleachComics.txt")));
		Iterator<String>it=urls.iterator();
		while(it.hasNext())
		{
			pw.println(it.next());
			pw.flush();
		}
		pw.close();
	}
	//从每个页面中解析得到每个漫画的对应图片url返回该list
	private static List<String> getAllUrlFromPerPage(List<String> list) throws IOException {
		List<String>urls=new ArrayList<String>();
		Iterator<String>it=list.iterator();
		Connection conn=null;
		Document document=null;
		int index=0;
		while(it.hasNext())
		{
			String page=it.next();
			conn=Jsoup.connect(page);
			document=conn.get();
			Elements tables=document.getElementsByTag("table");
//			String regex="\"([^\"]+[(jpg)*(png)*])";
			String regex="\"([^\"]+(jpg|png|JPG|PNG))";
			for(Element table:tables)
			{
				if(table.attr("border").equals("1"))
				{
					Element script=table.getElementsByTag("script").first();
					String scriptContent=script.html();
					Pattern p=Pattern.compile(regex);
					Matcher matcher=p.matcher(scriptContent);
					while(matcher.find())
					{
						String url="http://n.kukudm.com/"+matcher.group(1);
						System.out.println("解析出了第"+(++index)+"页："+url);
						urls.add(url);
						break;
					}
					break;
				}
			}
		}
		return urls;
	}
	//返回page列表。
	private static List<String> getPageList(int pageSize) throws IOException {
		List<String>list=new ArrayList<String>();
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("bleach.txt")));
		String sourceUrl=br.readLine();
		String prefix=sourceUrl.substring(0,sourceUrl.lastIndexOf("/")+1);
		for(int i=1;i<=pageSize;i++)
		{
			list.add(prefix+i+".htm");
		}
		br.close();
		return list;
	}

	//返回该漫画一共有多少页
	private static int getPageSize() throws IOException {
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("bleach.txt")));
		String url=br.readLine();
		br.close();
		Connection conn=Jsoup.connect(url);
		Document document=conn.get();
		Elements tables=document.getElementsByTag("table");
		for(Element table:tables)
		{
			if(table.attr("border").equals("1"))
			{
				String str=table.html();
//				System.out.println(str);
				String regex="共(\\d+)页";
				Pattern p=Pattern.compile(regex);
				Matcher matcher=p.matcher(str);
				while(matcher.find())
				{
					String pageSize=matcher.group(1);
					System.out.println("一共有"+pageSize+"页漫画！");
					return Integer.parseInt(pageSize);
				}
			}
		}
		return 0;
	}
}
