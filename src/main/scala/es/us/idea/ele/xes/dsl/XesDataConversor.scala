package es.us.idea.ele.xes.dsl

import java.io.{ByteArrayOutputStream, File, OutputStream}
import java.sql.Date
import java.util

import es.us.idea.adt.data.chameleon.internal.utils
import org.deckfour.xes.model.{XAttribute, XEvent, XLog, XTrace}
import org.deckfour.xes.model.impl._
import org.deckfour.xes.out.XesXmlSerializer

import scala.collection.mutable

object XesDataConversor {

  import scala.collection.JavaConverters._

  def chameleon2xes(a: Any): XLog = {
    val log = new XLogImpl(new XAttributeMapImpl())
    // De aqui tiene que salir un XLog
    utils.unwrap(a) match {
      case seq: Seq[Any] => {
        // devolver Xlog
        // cada elemento del array es un xtrace
        seq.foreach(x => {
          processTrace(x) match {
            case Some(tr) => log.add(tr)
            case _ => None
          }
        })
      }
      case _ => None
    }
    log
  }

  private def processTrace(anyTrace: Any): Option[XTrace] = {

    utils.unwrap(anyTrace ) match {
      case traceMap: Map[String, Any] => {
        utils.unwrap(traceMap.get("trace")) match {
          case traceGetMap: Map[String, Any] => {
            val id = utils.unwrap(traceGetMap.get("id")).toString
            val xtrace = new XTraceImpl(new XAttributeMapImpl(Map[String, XAttribute](
              "concept:name" -> new XAttributeLiteralImpl("concept:name", id)
            ).asJava))
            val events = utils.unwrap(traceGetMap.get("event"))
            events match {
              case seq: Seq[Any] => {
                seq.foreach(x => {
                  processEvent(x) match {
                    case Some(ev) => xtrace.add(ev)
                    case _ => None
                  }
                })
              }
              // if seq is not defined it means that this trace has not events
            }
            Some(xtrace)
          }
          case _ => None
        }
      }
      case _ => None
    }

  }

  private def processEvent(anyEvent: Any): Option[XEvent] = {
    utils.unwrap(anyEvent) match {
      case eventMap: Map[String, Any] => {
        // If the name or the timestamp is not defined, the event is None
        utils.unwrap(eventMap.get("name")) match {
          case None => None
          case name => {
            utils.unwrap(eventMap.get("timestamp")) match {
              case date: Date => {
                // Both the name and timestamp are defined. let's create the XEvent and check for other properties.
                val xEvent = new XEventImpl()
                val map: mutable.Map[String, XAttribute] = mutable.Map(
                  "concept:name" -> new XAttributeLiteralImpl("concept:name", name.toString),
                  "concept:timestamp" -> new XAttributeTimestampImpl("time:timestamp", date)
                )
                utils.unwrap(eventMap.get("resource")) match {
                  case None => None
                  case resource => map.put("org:resource", new XAttributeLiteralImpl("org:resource", resource.toString))
                }
                utils.unwrap(eventMap.get("resource")) match {
                  case None => None
                  case resType => map.put("Type", new XAttributeLiteralImpl("Type", resType.toString))
                }
                utils.unwrap(eventMap.get("costs")) match {
                  case None => None
                  case costs => map.put("Costs", new XAttributeLiteralImpl("Costs", costs.toString))
                }
                utils.unwrap(eventMap.get("customer")) match {
                  case None => None
                  case customer => map.put("Customer", new XAttributeLiteralImpl("Customer", customer.toString))
                }

                xEvent.setAttributes(new XAttributeMapImpl(map.asJava))
                Some(xEvent)
              }
              case _ => None
            }
          }
        }
      }
      case _ => None
    }
  }

  def xLogToString(xlog: XLog) = {
    val serializer = new XesXmlSerializer()
    val baou = new ByteArrayOutputStream
    serializer.serialize(xlog, baou)
    baou.toString()
  }

}
