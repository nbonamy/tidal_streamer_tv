package fr.bonamy.tidalstreamer.utils

class Configuration {

  fun getHttpBaseUrl(): String {
    return "${getHttpProtocol()}${getServerHostname()}:${getHttpPort()}"
  }

  fun getWsBaseUrl(): String {
    return "${getWsProtocol()}${getServerHostname()}:${getWsPort()}"
  }

  fun getServerHostname(): String {
    return "192.168.1.2"
  }

  fun getHttpProtocol(): String {
    return "http://"
  }

  fun getWsProtocol(): String {
    return "ws://"
  }

  fun getHttpPort(): Int {
    return 5002
  }

  fun getWsPort(): Int {
    return 5003
  }

}
