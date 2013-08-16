package guardbee.services.providers

import play.api.cache.Cache
import play.api.Play.current

trait BaseCacheStore {

  def getItem[T, K](collection: String, itemId: K): Option[T] = {
    Cache.getAs[Map[K, T]](collection).getOrElse(Map[K, T]()).get(itemId)
  }

  def saveItem[T, K](collection: String, item: T, itemId: K) = {
    val map = Cache.getAs[Map[K, T]](collection).getOrElse(Map()) ++ Map(itemId -> item)
    Cache.set(collection, map, 0)
  }

  def deleteItem[T, K](collection: String, itemId: K) = {
    val map = Cache.getAs[Map[K, T]](collection).getOrElse(Map()) - itemId
    Cache.set(collection, map, 0)
  }
}