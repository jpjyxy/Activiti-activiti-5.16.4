package com.activiti.plugin.component;

import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintVariable implements JavaDelegate
{
    public static Logger log = LoggerFactory.getLogger(PrintVariable.class);
    
    @Override
    public void execute(DelegateExecution execution)
        throws Exception
    {
        Map<String, Object> processInstance = execution.getVariables();
        log.info("当前流程的实例变量 {}", processInstance);
    }
    
}
