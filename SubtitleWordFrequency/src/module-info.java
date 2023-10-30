module SubtitleWordFrequency{
	requires java.desktop;
	requires org.apache.commons.io;
	requires com.google.gson;
	requires org.apache.commons.text;
	requires org.apache.commons.lang3;
	requires java.logging;
	requires java.management;
	requires java.naming;
	
	exports gui;
	exports SubtitleWordFrq;
	opens SubtitleWordFrq to com.google.gson;
}