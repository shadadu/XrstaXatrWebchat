package com.xrstaxatrwebchat.wchat.Utils;

import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;

import org.springframework.context.annotation.Bean;

import javax.naming.Context;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;


public class CreateNetwork {


    @Bean
    public ComputationGraph loadGraphFromMemory(Context ctx) throws Exception{

        Logger compGraphLogger = Logger.getLogger("CompGraph-logger");

        String modelFileName = (String) ctx.lookup("java:comp/env/MODEL_FILENAME");
        File networkFile = new File(new FileLoads().toTempPath(modelFileName, ctx));
        compGraphLogger.info("Network file path: "+networkFile.getPath());

        if (networkFile.exists()) {
            compGraphLogger.info("Loading the existing network...");
            try{
                ComputationGraph computationGraph = ModelSerializer.restoreComputationGraph(networkFile);
                compGraphLogger.info("Successfully loaded network file from path: "+networkFile.getPath());
                return computationGraph;
            }catch (IOException e){
                Logger.getLogger("Failed to load existing network file - file path "+networkFile.getPath());
                e.printStackTrace();
                throw new IOException("Failed to load the network file");
            }
        } else {
            compGraphLogger.info("Error: Network file does not exist");

            return null;
        }
    }

}
