# What's peace?
Peace is bridge between Market, RMS (the appliance builder) and Archipel in Xamin stack. Peace also is Xamin appliance repository in XMPP protocol. Before I explain what are actions that peace provide for each entity, lets list entities:

* **market** provides a web based UI for users to browse available appliances and install them on their xamin os.
* **RMS** registers available appliances to peace using xmpp.
* **xamin os (archipel)** it's where appliances get installed.

### Market
market and peace share data using master-slave setup of redis. market will be slave which needs just readonly access and peace will be master (btw market can use xmpp API that we will explain in `xamin os` part to query peace). if user uses web UI to install / remove appliance on xamin, market will send one of following stanzas to peace and peace will tell the market todo so:

```xml
    <!-- to remove an appliance -->
    <iq type="set" to="peace@hostname/resource">
        <remove from="jid-of-market@hypervisor.com" xmlns="market:xamin">
            <appliance version="x.y.z">foo</appliance>
        </remove>
    </iq>
    <!-- to install an appliance -->
    <iq type="set" to="peace@hostname/resource">
        <install to="jid-of-market@hypervisor.com" xmlns="market:xamin">
            <appliance version="x.y.z">foo</appliance>
        </install>
    </iq>
```

### RMS

#### set appliance
RMS makes appliances and notifies peace about availability of them. here's how it notifies peace

```xml
    <iq type="set" to="peace@hostname">
        <appliance xmlns="appliance:set:xamin">
            <name>foo</name>
            <version>x.y.z</version>
            <description>blah blah</description>
            <author>...</author>
            <url>http://somewhere/path/to/filename.xvm2</url>
            <tags>
                <tag>blah</tag>
                <tag>bar</tag>
                <tag>baz</tag>
            </tags>
            <cpu>2</cpu>
            <memory>512</memory>
            <storage>4</storage>
            <icon>cdn/relative/path/foo.png</icon>
            <images>
                <image>
                    <path>cdn/relative/path/foo/image1.png</path>
                    <title>title of first image</title>
                    <description>description of first image</description>
                </image>
                <image>
                    <path>cdn/relative/path/foo/image2.png</path>
                    <title>title of second image</title>
                    <description>description of second image</description>
                </image>
            </images>
            <category>category-name</category>
            <home>http://homepageofproject/</home>
            <payment>
                <stock>10000</stock>
                <off>
                    <percent>0.5</percent>
                    <incase>
                        <appliance version="a.b.c">baz</appliance>
                    </incase>
                </off>
            </payment>
        </appliance>
    </iq>
```

and peace will reply following stanza after registering the appliance:

```xml
     <iq type="result" to="rms@hostname" />
```

#### enable appliance
also after finishing image creation, RMS should call enable request:

```xml
           <iq type="set" to="peace@hostname">
               <appliance xmlns="appliance:enable:xamin">
                   <name>foo</name>
                   <version>x.y.z</version>
               </appliance>
           </iq>
```

and the response (in success) will be:

```xml
    <iq xmlns='jabber:client' from='peace@hostname' type='result'>
         <appliance ns='appliance:enable:xamin'/>
    </iq>
```

### Xamin OS
actually it's main client that queries the repository. here's are available actions:

#### search
search repository for available appliances by appliance name:

```xml
    <!-- request -->
    <iq type="get" to="peace@hostname">
        <search xmlns="client:search:xamin" query="foo*" />
    </iq>
    <!-- response -->
    <iq type="result" to="rms@hostname">
        <appliance>
            <name>foo</name>
            <version>x.y.z</version>
            <description>...</description>
            <author>...</author>
        </appliance>
        <appliance>
            <name>foobar</name>
            <version>u.v.w</version>
            <description>...</description>
            <author>...</author>
        </appliance>
    </iq>
```

or by file name: 

```xml
    <!-- request -->
    <iq type="get" to="peace@hostname">
        <search xmlns="client:hashsearch:xamin">
            <appliance hash="filenameA" />
            <appliance hash="filenameB" />
        </search>
    </iq>
    <!-- response -->
    <iq type="result" to="rms@hostname">
        <appliance>
            <name>foo</name>
            <version>x.y.z</version>
            <description>...</description>
            <author>...</author>
        </appliance>
        <appliance>
            <name>foobar</name>
            <version>u.v.w</version>
            <description>...</description>
            <author>...</author>
        </appliance>
    </iq>
```

#### get
get full information about an appliance (everything which will be provided in "set" except url of download).

```xml
    <!-- request -->
    <iq type="get">
        <appliance xmlns="appliance:get:xamin">
            <name>foo</name>
            <version>x.y.z</version>
        </appliance>
    </iq>
    <iq type="result">
        <appliance xmlns="appliance:get:xamin">
            <name>foo</name>
            <description>blah blah foo blah blah</description>
            <version>x.y.z</version>
            <author>...</author>
            <!-- other properties -->
        </appliance>
    </iq>
```

#### subscribe/unsibscribe
subscribtion / unsubscribetion is based on pubsub extension of xmpp. the only point is that the pub node will be name of appliance like: foo

#### install
install command is combination of get and subscribe which peace@hostname will do the subscribetion by itself (has admin privilages).

```xml
    <!-- request -->
    <iq type="set">
        <install xmlns="appliance:install:xamin">
            <name>foo</name>
            <version>x.y.z</version>
            <!-- optional - older version already installed -->
            <base>x.y.y</base>
        </install>
    </iq>
    <!-- response which is a get response -->
    <iq type="result">
        <appliance xmlns="appliance:install:xamin">
            <name>foo</name>
            <description>blah blah foo blah blah</description>
            <version>x.y.x</version>
            <url>ftp://hostname/path/to/foo.x.y.z</url>
            <author>...</author>
        </result>
    </iq>
```

### Development
peace uses Smack XMPP library and to reply to a packet in Smack you need to go through these steps:

* register a Packet provider based on namespace/element (or something else) to generate a Packet instance (usually an IQ) from received XML stream
* register a Packet Listener to process Packet Instances

things are a little bit more orginized in source code which we'll get into that next but the above steps are what you should do in Smack.

#### provider
as I mentioned before a Provider makes a Packet from XML Stream. you should put your provider in src/main/scala/ir/xamin/providers/ and register it to ProviderManager in registerIQProviders method of src/main/scala/ir/xamin/Peace.scala.

#### Received Packet
to make a Packet instance in provider you need to Make a class for your packet which normaly extends org.jivesoftware.smack.packet.IQ. put your Packet in src/main/scala/ir/xamin/packet/receive/.

#### Processor
to process the Packets that Provider generates we need to Register a Packet Listener in registerProcessors method of src/main/scala/ir/xamin/Peace.scala. the Packet listener will receive instance of XMPP Connection and redis to do the process it needs to done for specific packet. but we need to tell the listener to listen to what packets, to do that create a filter property based on org.jivesoftware.smack.filter.PacketFilter. also you should put your Packet Processor in src/main/scala/ir/xamin/processors/.

#### Reply Packet
after the process got done, we need to reply to the received packet. to do so create the createResultIQ method in Received Packet and put your reply packet in src/main/scala/ir/xamin/packet/reply/ then call the createResultIQ on received packet instance that you got in the processor.

#### example
take a look at 4d235be5afe742f37849036ab39236ac910259c5 commit.

#### sbt
sbt is build tool that we use for development of peace. here's some useful commands:

* **~compile** to compile source by each change
* **run** to run peace via sbt
* **one-jar** to make a portable jar file
