package com.tempo.application.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggerUtils {
    
    /**
     * Obtient un Logger pour la classe spécifiée
     * @param clazz La classe pour laquelle créer le logger
     * @return Le Logger créé
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Log une information
     * @param logger Le logger à utiliser
     * @param message Le message à logger
     */
    public static void info(Logger logger, String message) {
        logger.info(message);
    }
    
    /**
     * Log un avertissement
     * @param logger Le logger à utiliser
     * @param message Le message à logger
     */
    public static void warning(Logger logger, String message) {
        logger.warn(message);
    }
    
    /**
     * Log une erreur
     * @param logger Le logger à utiliser
     * @param message Le message à logger
     */
    public static void error(Logger logger, String message) {
        logger.error(message);
    }
    
    /**
     * Log une erreur avec son exception
     * @param logger Le logger à utiliser
     * @param message Le message à logger
     * @param e L'exception à logger
     */
    public static void error(Logger logger, String message, Throwable e) {
        logger.error(message, e);
    }
}