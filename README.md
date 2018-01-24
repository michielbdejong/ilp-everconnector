# ilp-everconnector
Minimal PoC ILP Connector supporting plugins and easy configuration.

As of 2018-01-23 next branch of hyperledger quilt is required to compile withour errors: 

    https://github.com/hyperledger/quilt/tree/earizon-add_missing_packets

`org/everis/interledger/tools` contains mock objects useful for development 
(probably it would be better to place in its own project as is the case for plugin).

An example network  `connector1 <- http-over-ilp -> connector2 <-> webshop` is available at `org/everis/interledger/tools/mockILPNetwork/
`
Please, read dev_docs notes for more info about the architecture and other developer's notes, TODOs. ...

Note: TODOs in code are marked in decreasing order of importance: 

    TODO:(0)   -> FIXME
    TODO:(0.5) -> FIXME after fixing TODO:(0) 
    TODO:(1)   -> Improvement
    TODO:(2)   -> Will never be done, but it's good to dream about it.
    
# Installing:
Maven is used for building/deployment.

All maven artifact/dependencies are available in Maven public repositories except the development branch of Quilt (SNAPSHOT). To install it to the local maven repository of the development machine:

    # clone custom Quilt branch with non-merged fixes
    $ git clone https://github.com/hyperledger/quilt -b earizon-add_missing_packets
    $ cd quilt
    # Quick install to maven local repository
    $ ./dev-ops/util/quick_local_install_mvn_package.sh
    
(Hopefully this manual install will dissapear in a near future once changes are merged to master branch)

Once this Quilt SNAPSHOT is installed locally, use your favourite IDE with Maven support (IntelliJ, Eclipse, NetBeans, ...) to compile the application.

# Configuration:
The configuration is done through standard java properties files (key=value). Wrappers are used to parse/validate the configuration using a fail-fast approach (abort launch until config is OK). Check `PropertiesConfig.java`, `BasePluginConfig.java`, `ILPOverHTTPConfig.java`, `ConnectorConfig.java` for more info.

# Setting up a test network:
Check entry point (== `public static void main(String[] args`) at `org.everis.interledger.tools.NetworkWithTwoConnectors.java` for an example of how to setup a local network with two connectors and a webshop "listening" on the second one. Check related config files for examples on how to configure static routes.


