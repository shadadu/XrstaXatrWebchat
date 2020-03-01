package com.xrstaxatrwebchat.wchat;

import com.xrstaxatrwebchat.wchat.Utils.CorpusProcessor;
import com.xrstaxatrwebchat.wchat.Utils.CreateNetwork;
import com.xrstaxatrwebchat.wchat.Utils.GetInitContext;
import com.xrstaxatrwebchat.wchat.Utils.Seq2SeqModel;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.Context;
import javax.naming.NamingException;

@Configuration
@EnableConfigurationProperties
public class AppConfigs{

    @Bean
    Seq2SeqModel runInstance() {
        return new Seq2SeqModel();
    }

    @Bean
    ComputationGraph netInstance() throws Exception {

        ComputationGraph compNet = null;
        try {


            Context contx = new GetInitContext().fun();

            ClassLoader contxClassLoader = Thread.currentThread().getContextClassLoader();

        	compNet = new CreateNetwork().loadGraphFromMemory(contx);

        	try{
        	    if(contx.getClass().getClassLoader() == contxClassLoader){
        	        contx.close();
                }
            }catch(NamingException ne){
        	    System.out.println("Error removing contexloader "+ne.getExplanation());
        	    ne.printStackTrace();
            }

        	if(compNet==null) throw new Exception("loaded null graph file");
        }catch(Exception ioe) {
        	ioe.printStackTrace();
        	throw new Exception("Failed to load the comp graph");
        }
        
        return compNet;
    }

    @Bean
    CorpusProcessor corpusProcessorInstance() {

        try {

            Context context = new GetInitContext().fun();
            ClassLoader contxClassLoader = Thread.currentThread().getContextClassLoader();

            CorpusProcessor corpusProcessor = new Seq2SeqModel().createDictionary(context);

            try{
                if(context.getClass().getClassLoader() == contxClassLoader){
                    context.close();
                }
            }catch(NamingException ne){
                System.out.println("Error removing contexloader "+ne.getExplanation());
                ne.printStackTrace();
            }

            return corpusProcessor;

        } catch (Exception e) {
            System.out.println("Failed to create dictionary & corpus");
            e.printStackTrace();
            return null;
        }
    }

}