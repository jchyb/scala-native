import java.nio.file.FileSystems
object Test {
  def main(args: Array[String]): Unit = {
    FileSystems.getDefault().getPath(".").normalize()
    //val a = Array(1, 2).toSeq.view.sorted.sorted
    
  }
}
