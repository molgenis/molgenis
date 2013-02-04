<#--helper functions-->
<#include "CPPHelper.ftl">

<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* Date:        ${date}
 * 
 * generator:   ${generator} ${version}
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */

#include <cstdlib>
#include <cstdio>
#include <string>
#include <vector>
#include <jni.h>
#include "MolgenisServlet.h"
<#list model.entities as entity>
#include "${entity.namespace?replace(".", "/")}/${JavaName(entity)}.h"
</#list>

using namespace std;

char* classpath = (char*)"-Djava.class.path=${UserHome}/build/classes;${UserHome?replace("_apps","")}/bin;${UserHome?replace("_apps","")}/build;";

JNIEnv* create_vm(JavaVM** jvm) {
  JNIEnv* env;
  JavaVMInitArgs vm_args;
  JavaVMOption options;
  options.optionString = classpath;
  
  vm_args.version = JNI_VERSION_1_6; 						//JDK version. This indicates version 1.6
  vm_args.nOptions = 1;
  vm_args.options = &options;
  vm_args.ignoreUnrecognized = 0;

  int ret = JNI_CreateJavaVM(jvm, (void**)&env, &vm_args);
  if(ret < 0)
    printf("\nUnable to Launch JVM\n");       
  return env;
}

void test_mappings(JNIEnv* env){
    <#list model.entities as entity>
    <#if !entity.abstract>
  	//${JavaName(entity)}* test${entity_index} = 
  	new ${JavaName(entity)}(env);
  	</#if>
  	</#list>
}

int main(int argc, char* argv[]){
  JNIEnv* env;
  JavaVM* jvm;
  env = create_vm(&jvm);
  if(!(env == NULL)){
    MolgenisServlet* servlet = new MolgenisServlet(env);
  	jobject db = servlet->getDatabase();
  	Investigation* investigation = new Investigation(env);
    investigation->findByName(db,"dfsfdsdfsdfsdf");
    investigation->findById(db,"1");
    investigation->find(db);
  }
  jvm->DestroyJavaVM();
}   
