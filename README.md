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
    
