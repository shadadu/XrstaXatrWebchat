package com.xrstaxatrwebchat.wchat.Utils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class GetInitContext {

    public Context fun(){
        try{
            return new InitialContext();
        }catch (NamingException nme){
            nme.printStackTrace();
            return null;
        }
    }


}
