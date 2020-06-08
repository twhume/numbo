(defproject numbo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
  															[org.clojure/clojure "1.10.0"]
  															[org.clojure/tools.logging "1.1.0"]
  															[random-seed "1.0.0"]
  															[rhizome "0.2.9"]
  															[seesaw/seesaw "1.5.0"]]
  :main ^:skip-aot numbo.core
  :repl-options {:init-ns numbo.core})
