package com.softwaremill.id.pretty

import com.softwaremill.id.{DefaultIdGenerator, IdGenerator}

/**
  * It makes Long ids more readable and user friendly, it also adds checksum.
  *
  * @param encoder      it the result needs to be monotonic, use monotonic Coded e.g. AlphabetCoded with alphabet where char values are monotonic
  * @param partsSize    the long is chopped on the parts, here you specify the part length (only even parts are encoded with codec)
  * @param delimiter    sign between parts
  * @param leadingZeros prettifier will make id with constant length
  */
class IdPrettifier private(
    encoder: Codec,
    partsSize: Int,
    delimiter: Char,
    leadingZeros: Boolean
) {

  val zeroChar         = encoder.encode(0).charAt(0)
  val maxEncodedLength = encoder.encode(scala.math.pow(10, partsSize).toLong - 1).length

  def prettify(idSeed: Long): String = {
    require(idSeed >= 0)
    val parts = divide(Damm(idSeed.toString))
    val partsToConvert = withLeadingZeros(parts) { ps =>
      addLeadingZerosParts(ps)
    }
    convertParts(partsToConvert)
  }

  def isValid(id: String): Boolean = Damm.isValid(decodeSeedWithCheckDigit(id))

  def toIdSeed(id: String): Either[ConversionError, Long] = convertToLong(id)

  private def divide(s: String): Seq[String] =
    s.reverse.grouped(partsSize).toSeq.reverse.map(_.reverse)

  private def addLeadingZerosParts(parts: Seq[String]): Seq[String] = {
    val maxParts = scala.math.ceil(20d / partsSize.toDouble).toInt
    parts.reverse.padTo(maxParts, "0").reverse
  }

  case class ConversionError(invalidId: String)

  private def convertToLong(s: String): Either[ConversionError, Long] = {
    val decodedWithCheckDigit: String = decodeSeedWithCheckDigit(s)
    if (Damm.isValid(decodedWithCheckDigit)) {
      try {
        Right(decodedWithCheckDigit.dropRight(1).toLong)
      } catch {
        case e: NumberFormatException =>
          Left(ConversionError(s))
      }
    } else {
      Left(ConversionError(s))
    }
  }

  private def withLeadingZeros[T](t: T)(forLeadingZeros: (T => T)): T =
    if (leadingZeros) forLeadingZeros(t) else t

  private def convertParts(parts: Seq[String]): String =
    parts
      .foldRight(Seq[String]()) { (part, result) =>
        val isEven = result.length % 2 == 0
        if (isEven) {
          val convertedPart = withLeadingZeros(part) { p =>
            addLeadingZeros(p, '0', partsSize)
          }
          Seq(convertedPart) ++ result
        } else {
          val encoded = encoder.encode(part.toInt)
          val convertedPart = withLeadingZeros(encoded) { e =>
            addLeadingZeros(e, zeroChar, maxEncodedLength)
          }
          Seq(convertedPart) ++ result
        }
      }
      .mkString(delimiter.toString)

  private def addLeadingZeros(encodedPart: String, zeroChar: Char, maxPartSize: Int): String =
    encodedPart.reverse.padTo(maxPartSize, zeroChar).reverse

  private def decodeSeedWithCheckDigit(s: String) = {
    val parts = s.split(delimiter)
    val decodedWithCheckDigit = parts
      .foldRight(Seq[String]()) { (part, result) =>
        val isEven = result.length % 2 == 0
        if (isEven) {
          Seq(part) ++ result
        } else {
          val decoded = addLeadingZeros(encoder.decode(part).toString, '0', partsSize)
          Seq(decoded.toString) ++ result
        }
      }
      .mkString
    decodedWithCheckDigit
  }
}

object IdPrettifier {
  val default = new IdPrettifier(new AlphabetCodec(Alphabet.Base23), 5, '-', true)
  def custom(
      encoder: Codec = new AlphabetCodec(Alphabet.Base23),
      partsSize: Int = 5,
      delimiter: Char = '-',
      leadingZeros: Boolean = true
  ) = new IdPrettifier(encoder, partsSize, delimiter, leadingZeros)
}

object PrettyIdGenerator {
  val singleNode = new PrettyIdGenerator(new DefaultIdGenerator(), IdPrettifier.default)
  def distributed(workerId: Long, datacenterId: Long) =
    new PrettyIdGenerator(new DefaultIdGenerator(workerId, datacenterId), IdPrettifier.default)
}

class PrettyIdGenerator(idGenerator: IdGenerator, idPrettifier: IdPrettifier) extends StringIdGenerator {

  import idPrettifier._

  def nextId(): String = prettify(idGenerator.nextId())

  def idBaseAt(timestamp: Long): String = prettify(idGenerator.idBaseAt(timestamp))
}
