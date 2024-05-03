package it.unipi.accessroads.utils

enum class FilterType {
    NONE, LOW_PASS
}
class TimeSeries(val size:Int) {
    private val FILTER_WINDOW=3//must be dispari
    private val data = FloatArray(size)
    private var currentIndex = 0

    fun addValue(sensorData: Float) {
        data[currentIndex] = sensorData
        currentIndex = (currentIndex + 1) % size
    }

    fun getWindowToAnalize(windowSize: Int,filter:FilterType=FilterType.NONE): FloatArray {
        when(filter){
            FilterType.NONE->{val recentData = FloatArray(windowSize)
                for (i in 0 until windowSize) {
                    val index = (currentIndex - windowSize + i + size) % size
                    recentData[i] = data[index]
                }
                return recentData
            }
            FilterType.LOW_PASS->{
                val temp=FloatArray(windowSize+FILTER_WINDOW-1)
                val recentData=FloatArray(windowSize)
                for(i in 0 until windowSize+FILTER_WINDOW-1){
                    val index=(currentIndex - windowSize + i + size-(FILTER_WINDOW-1)) % size
                    temp[i]=data[index]
                }
                for(i in (FILTER_WINDOW-1)/2 until windowSize+(FILTER_WINDOW-1)/2){
                    val window=FloatArray(FILTER_WINDOW)
                    System.arraycopy(temp,i-(FILTER_WINDOW-1)/2,window,0,FILTER_WINDOW)
                    recentData[i-(FILTER_WINDOW-1)/2]=window.average().toFloat()
                }
                return recentData
            }
        }
    }
}