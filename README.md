# rocket-core-data-network
![example workflow](https://github.com/Rocket-Beer/rocket-core-data-network/actions/workflows/android.yml/badge.svg)

Core library to manage network access

## Preconditions
Conect to repository with GitHub credentials located in "local.properties" archive and config maven properties in "settings.gradle"

local.proterties has to contain:
> github.username=*******  
> github.token=*********************

settings.gradle has contain Rocket-Beer connection with maven
~~~
maven {  
    Properties properties = new Properties()  
    properties.load(file('local.properties').newDataInputStream())  
    url "https://maven.pkg.github.com/Rocket-Beer/*"  
    
    credentials {  
        username = properties.getProperty("github.username")  
        password = properties.getProperty("github.token")  
    }  
}
~~~

## User manual
### Implement Rocket data packages
* Package "core-data-network-commons" is optional
~~~
implementation "com.rocket.core:core-data-network-commons:0.0-beta0"
implementation "com.rocket.android.core:core-data-network:0.0.1-beta"
implementation "com.rocket.android.core:core-data-network-test:0.0.1-beta"
~~~

### Execute a generic api call (non BaseNetworkApiResponse response expected).
~~~
requestGenericSuspendApi(
    call = { 
        //Call to api service
    },
    parserSuccess = {
        //Manage service response
    }
)
~~~
~~~
requestGenericApi(
    call = { 
        //Call to api service
    },
    parserSuccess = {
        //Manage service response
    }
)
~~~

### Execute an api call (BaseNetworkApiResponse response expected).  
* Parameter "parserError" is optional.
~~~
requestSuspendApi(
    call = { 
        //Call to api service
    },
    parserSuccess = {
        //Manage service response
    }
)
~~~
~~~
requestApi(
    call = { 
        //Call to api service
    },
    parserSuccess = {
        //Manage service response
    },
    parserError = {
        //Manage error response
    }
)
~~~

## Packages
core-data-network-commons --> 0.0-beta0
core-data-network --> 0.0.1-beta
core-data-network-test --> 0.0.1-beta
