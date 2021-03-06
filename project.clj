(defproject numbo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[clj-log4j2 "0.2.0"]
  															[org.clojure/clojure "1.10.0"]
																	[org.clojure/tools.cli "1.0.194"]
  															[org.clojure/tools.logging "1.1.0"]
  															[random-seed "1.0.0"]
  															[rhizome "0.2.9"]
  															[seesaw/seesaw "1.5.0"]]
  :main ^:skip-aot numbo.core
  :repl-options {:init-ns numbo.core}
  :plugins [[jonase/eastwood "0.3.10"]])
