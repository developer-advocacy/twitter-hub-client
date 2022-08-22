# The Twitter Gateway Client

This provides an autoconfiguration and client to work with the [`twitter-service`](https://github.com/developer-advocacy/twitter-gateway) 
I built to act as a gateway or proxy for all outbound Twitter tweets. 

## Usage 

* Add dependency to your Spring Boot application's classpath: `com.joshlong:twitter-gateway-client:${version}`
* Acquire client credentials from the Twitter service (you'll need to install them manually into the `twitter_clients` SQL database for the moment)
* Then inject and use the `Twitter` object furnished by the autoconfiguration



