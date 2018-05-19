/*
 Copyright 2018 Peter-Josef Meisch (pj.meisch@sothawo.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.sothawo.twilikt

import com.vaadin.spring.annotation.SpringComponent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */

@Target(AnnotationTarget.PROPERTY)
annotation class Slf4jLogger

@SpringComponent
class LoggingInjector : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        try {
            bean.let {
                val loggerName = it::class.java.canonicalName
                if (loggerName != null) {
                    processObject(it, loggerName)
                    it::class.companionObjectInstance?.let {
                        processObject(it, loggerName)
                    }
                }
            }
        } catch (ignored: Error) {
            // ignore exceptions, keep the object as it is. not every required class may be found on the classpath as
            // SpringBoot tries to load notexisting stuff as well
        }

        return bean
    }

    private fun processObject(target: Any, loggerName: String) {
        target::class.declaredMemberProperties.forEach { property ->
            property.annotations
                    .filter { it is Slf4jLogger }
                    .forEach {
                        if (property is KMutableProperty<*>) {
                            property.setter.call(target, LoggerFactory.getLogger(loggerName))
                        }
                    }
        }
    }
}
