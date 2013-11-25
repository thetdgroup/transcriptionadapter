package com.thetdgroup.configurations;

import org.jdom.Element;
import org.jdom.JDOMException;

import edu.cmu.sphinx.util.props.ConfigurationManager;

public class Sphinx4Model
{
 private ConfigurationManager configurationManager = null;
 
 //
 public void parse(final Element element) throws JDOMException
 {
  configurationManager = new ConfigurationManager(element.getChildText("file"));
 }

 public ConfigurationManager getConfigurationManager()
 {
  return configurationManager;
 }

 
}
