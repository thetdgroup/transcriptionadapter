package com.thetdgroup;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.json.JSONObject;

import com.thetdgroup.configurations.Sphinx4Model;

//import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
//import edu.cmu.sphinx.linguist.language.grammar.TextAlignerGrammar;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.sound.sampled.UnsupportedAudioFileException;
 
//
public class TranscriptionAdapter extends BaseTranscriptionAdapter 
{
	private static String configurationFile = "";
 private static HashMap<String, Sphinx4Model> sphinxModels = new HashMap<String, Sphinx4Model>();
 
	private FuzeInCommunication fuzeInCommunication = new FuzeInCommunication();
	
	//
	public void initialize(final JSONObject configurationObject) throws Exception
	{
		if(configurationObject.has("adapter_configuration_file") == false)
		{
			throw new Exception("The adapter_configuration_file parameter was not found");
		}
		
		//
		if(configurationObject.has("fuzein_connection_info"))
		{
			JSONObject jsonCommParams = configurationObject.getJSONObject("fuzein_connection_info");
			
			fuzeInCommunication.setFuzeInConnection(jsonCommParams.getString("service_url"), 
																																											jsonCommParams.getInt("service_socket_timeout"), 
																																											jsonCommParams.getInt("service_connection_timeout"), 
																																											jsonCommParams.getInt("service_connection_retry"));
		}
		
		//
		parseAdapterConfiguration(configurationObject.getString("adapter_configuration_file"));		
	}
	
	//
	public void destroy()
	{
	 if(fuzeInCommunication != null)
	 {
	 	fuzeInCommunication.releaseConnection();
	 }
	}
	
	//
	public JSONObject transcribeFile(final JSONObject jsonData) throws Exception
	{
		JSONObject jsonResponse = new JSONObject();

		//
		// Validate that all required params are present
		//
  if(jsonData.has("audio_language") == false)
  {
   throw new Exception("The 'audio_language' parameter is required.");
  }

  if(jsonData.has("audio_file") == false)
  {
   throw new Exception("The 'audio_file' parameter is required.");
  }
		
  //
  String languageISO = jsonData.getString("audio_language");
  
  if(sphinxModels.get(languageISO) != null)
  {
   Sphinx4Model sphinxModel = sphinxModels.get(languageISO);
   jsonResponse = sphinx4Transcribe(sphinxModel, jsonData.getString("audio_file"));
  }
  else
  {
   throw new Exception(jsonData.getString("audio_language") + " is not supported or the models were not found.");
  }
		
	 //
  return jsonResponse;
	}
	
	//
	private JSONObject sphinx4Transcribe(final Sphinx4Model sphinxModel, final String audioFile) throws IOException, UnsupportedAudioFileException  
	{
	 JSONObject jsonResponse = new JSONObject();
	 
	 //
  Recognizer recognizer = (Recognizer) sphinxModel.getConfigurationManager().lookup("recognizer");

  TextAlignerGrammar Grammar = (TextAlignerGrammar) sphinxModel.getConfigurationManager().lookup("textAlignGrammar");
  recognizer.addResultListener(Grammar);

  // Allocate the Resource Necessary for the recognizer 
  recognizer.allocate();

  // Configure the audio input for the recognizer 
  AudioFileDataSource dataSource = (AudioFileDataSource) sphinxModel.getConfigurationManager().lookup("audioFileDataSource");
  dataSource.setAudioFile(new URL("file:" + audioFile), null);
  
  Result result = null;
  String resultTextAggregated = "";
  
  while((result = recognizer.recognize()) != null) 
  {
   String resultText = result.getTimedBestResult(false, true);
   System.out.println(resultText);
   
   resultTextAggregated += resultText;
  }
  
  System.out.println(resultTextAggregated + "\n");
  
  //
  return jsonResponse;
 }
	
	//
	private ArrayList<HashMap<String, ArrayList<Integer>>> parseAlignedText(String alignedText)
	{
  String[] wordsDurationsArray = alignedText.split(" ");
 
  for(int wordsCounter = 0; wordsCounter < wordsDurationsArray.length; wordsCounter++)
  {
   
  }
  
  //
  return null;
 }
	
	//
	@SuppressWarnings("unchecked")
	private void parseAdapterConfiguration(String adapterConfigurationFile) throws Exception
	{
		//
		// Parse Configuration
		configurationFile = adapterConfigurationFile;
		Document configurationDocument = saxBuilder.build(adapterConfigurationFile);
		
		//
		// Get for each language the appropriate models 
		XPath xPath = XPath.newInstance("sphinx4_configuration/languages");
		List<Element> elementsList = xPath.selectNodes(configurationDocument);
		Iterator<Element> serverIterator = elementsList.iterator();
		
		while(serverIterator.hasNext())
		{
			Element configurationElement = serverIterator.next();
			
			String iso3 = configurationElement.getAttributeValue("iso3");
			
			Sphinx4Model model = new Sphinx4Model();
   model.parse(configurationElement);
   
   sphinxModels.put(iso3, model);
		}
	}
}