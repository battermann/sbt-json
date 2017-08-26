package util

object CollectionUtils {
  /**
    *
    * @param collection
    * @param key
    * @param compare
    * @tparam T
    * @tparam K
    * @tparam V
    * @return Map of keys of duplicate objects. Value is the original object that is duplicated.
    */
  def findDuplicates[T, K, V](collection: Seq[T])(key: T => K, compare: (T, T) => Boolean): Map[K, T] = {
    val result =
      collection.foldLeft((Map[K, T](), collection.drop(1))) { case ((acc, others), current) =>
        val duplicates = others
          .filter(other => compare(current, other))
          .map(duplicate => (key(duplicate), current))
          .toMap

        (acc ++ duplicates, others.drop(1).filter(x => !duplicates.contains(key(x))))
      }

    result._1
  }
}
