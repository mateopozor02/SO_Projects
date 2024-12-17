// VirtualMemoryManagerV1.java
package vmmanager;

import vmsimulation.BackingStore;
import vmsimulation.BitwiseToolbox;
import vmsimulation.MainMemory;
import vmsimulation.MemoryException;

public class VirtualMemoryManagerV1 {

    MainMemory memory;    // The main memory
    BackingStore disk;    // The disk
    Integer pageSize;    // Page size
    PageTable pageTable;
    int numBits;
    int numPages;
    int nextFrameNum;
    int pageFaults;

    private int log2(int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    public VirtualMemoryManagerV1(MainMemory memory, BackingStore disk, Integer pageSize) throws MemoryException {
        this.memory = memory;
        this.disk = disk;
        this.pageSize = pageSize;
        numBits = log2(this.memory.size());
        numPages = disk.size() / pageSize;
        pageTable = new PageTable(numPages);
        nextFrameNum = 0;
        pageFaults = 0;
    }

    public void writeByte(Integer fourByteBinaryString, Byte value) throws MemoryException {
        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, numBits - 1);
        int pageNumber = address / pageSize;
        int offset = address % pageSize;
        pageTable.addToRam(pageNumber);
        int translatedAddress = pageTable.getDisplacement(pageNumber) + offset;
        memory.writeByte(translatedAddress, value);
        System.out.println("RAM: @" + BitwiseToolbox.getBitString(translatedAddress, numBits - 1) + " <-- " + value);
    }

    public Byte readByte(Integer fourByteBinaryString) throws MemoryException {
        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, numBits - 1);
        int pageNumber = address / pageSize;
        int offset = address % pageSize;
        pageTable.addToRam(pageNumber);
        int translatedAddress = pageTable.getDisplacement(pageNumber) + offset;
        byte valInAddr = memory.readByte(translatedAddress);
        System.out.println("RAM: @" + BitwiseToolbox.getBitString(translatedAddress, numBits - 1) + " --> " + valInAddr);
        return valInAddr;
    }

    public void printMemoryContent() throws MemoryException {
        for (int i = 0; i < memory.size(); i++) {
            System.out.println(BitwiseToolbox.getBitString(i, numBits - 1) + ": " + memory.readByte(i));
        }
    }

    public void printDiskContent() throws MemoryException {
        for (int i = 0; i < numPages; i++) {
            System.out.print("PAGE " + i + ": ");
            byte[] page = disk.readPage(i);
            for (int j = 0; j < page.length; j++) {
                System.out.print(page[j]);
                if (j < page.length - 1) {
                    System.out.print(",");
                }
            }
            System.out.println();
        }
    }

    public void writeBackAllPagesToDisk() throws MemoryException {
        for (int i = 0; i < numPages; i++) {
            if (pageTable.getMemoryState(i).getLoadedState()) {
                byte[] pageTemp = new byte[pageSize];
                int displacement = pageTable.getDisplacement(i);
                for (int j = 0; j < pageSize; j++) {
                    pageTemp[j] = memory.readByte(displacement + j);
                }
                disk.writePage(i, pageTemp);
            }
        }
    }

    public int getPageFaultCount() {
        return pageFaults;
    }

    public int getTransferedByteCount() {
        return pageFaults * pageSize * 2;
    }

    class PageTable {

        private MemoryState[] pages;
        private int numPages;

        public PageTable(int numPages) {
            this.numPages = numPages;
            this.pages = new MemoryState[numPages];

            for (int i = 0; i < numPages; i++) {
                pages[i] = new MemoryState();
            }
        }
        
        public void addToRam(int pageNum) throws MemoryException {
            if (pages[pageNum].getLoadedState()) {
                System.out.println("Page " + pageNum + " is in memory");
            } else {
                System.out.println("Bringing page " + pageNum + " into frame " + nextFrameNum);
                pages[pageNum].setLoadedState(true);
                byte[] page = disk.readPage(pageNum);

                for (int i = 0; i < pageSize; i++) {
                    memory.writeByte(nextFrameNum * pageSize + i, page[i]);
                }

                pages[pageNum].setDisplacement(nextFrameNum * pageSize);
                nextFrameNum++;
                pageFaults++;
            }
        }

        public int getDisplacement(int pageNum) {
            return pages[pageNum].getDisplacement();
        }

        public MemoryState getMemoryState(int pageIndex) {
            if (pageIndex < 0 || pageIndex >= numPages) {
                throw new IndexOutOfBoundsException("Invalid page index");
            }
            return pages[pageIndex];
        }
    }

    class MemoryState {

        private boolean pageLoaded = false;
        private int displacement;

        public boolean getLoadedState() {
            return pageLoaded;
        }

        public void setLoadedState(boolean loaded) {
            pageLoaded = loaded;
        }

        public void setDisplacement(int disp) {
            displacement = disp;
        }

        public int getDisplacement() {
            return displacement;
        }
    }
}
