package vmmanager;

import vmsimulation.*;
import java.util.LinkedList;

public class VirtualMemoryManagerV4 {
    MainMemory memory;
    BackingStore disk;
    Integer pageSize;
    BitwiseToolbox bToolbox = new BitwiseToolbox();
    int[] pageTable;
    LinkedList<Integer> pageQueue; // Used for LRU implementation
    int numOfPages;
    int numOfFrames;
    int bitCount;
    int pageFault = 0;
    int byteCount = 0;
    boolean[] dirtyBits;

    public VirtualMemoryManagerV4(MainMemory memory, BackingStore disk, Integer pageSize) throws MemoryException {
        this.memory = memory;
        this.disk = disk;
        this.pageSize = pageSize;
        this.numOfPages = this.disk.size() / this.pageSize;
        this.numOfFrames = this.memory.size() / this.pageSize;
        this.pageTable = new int[numOfPages];
        this.pageQueue = new LinkedList<>(); // For LRU
        this.bitCount = (int) (Math.log(this.memory.size()) / Math.log(2));
        this.dirtyBits = new boolean[numOfPages];

        for (int i = 0; i < pageTable.length; i++) {
            pageTable[i] = -1;
            dirtyBits[i] = false;
        }
    }

    public void writeByte(Integer virtualAddress, byte value) throws MemoryException {
        int pageNumber = virtualAddress / this.pageSize;

        if (this.pageTable[pageNumber] == -1) {
            handlePageFault(pageNumber);
        } else {
            updatePageAccess(pageNumber);
        }

        int frame = this.pageTable[pageNumber];
        int address = calculateAddress(frame, virtualAddress);
        memory.writeByte(address, value);

        // Set the dirty bit for this page
        dirtyBits[pageNumber] = true;

        System.out.println("RAM: @" + bToolbox.getBitString(address, bitCount - 1) + " <-- " + value);
    }

    public Byte readByte(Integer virtualAddress) throws MemoryException {
        int pageNumber = virtualAddress / this.pageSize;

        if (this.pageTable[pageNumber] == -1) {
            handlePageFault(pageNumber);
        } else {
            updatePageAccess(pageNumber);
        }

        int frame = this.pageTable[pageNumber];
        int address = calculateAddress(frame, virtualAddress);
        byte value = memory.readByte(address);
        System.out.println("RAM: @" + bToolbox.getBitString(address, bitCount - 1) + " --> " + value);
        return value;
    }

    private void updatePageAccess(int pageNumber) {
        pageQueue.remove((Integer) pageNumber);
        pageQueue.add(pageNumber);
    }

    private void handlePageFault(int pageNumber) throws MemoryException {
        int frame;
        if (pageQueue.size() < this.numOfFrames) {
            frame = pageQueue.size();
        } else {
            frame = evictPageFromMemory();
        }
        loadPageIntoMemory(pageNumber, frame);

        // Add the new page to the end of the queue (most recently used)
        pageQueue.add(pageNumber);

        pageFault++;
    }

    private int evictPageFromMemory() throws MemoryException {
        int evictedPage = pageQueue.removeFirst(); // Evict the least recently used page
        int frame = pageTable[evictedPage];

        if (dirtyBits[evictedPage]) {
            // Page is dirty; write back to disk
            System.out.println("Evicting page " + evictedPage);
            byte[] pageData = new byte[pageSize];
            int frameStartAddress = frame * pageSize;
            for (int offset = 0; offset < pageSize; offset++) {
                int physicalAddress = frameStartAddress + offset;
                pageData[offset] = memory.readByte(physicalAddress);
            }
            disk.writePage(evictedPage, pageData);
            byteCount += pageSize;
        } else {
            // Page is not dirty; do not write back to disk
            System.out.println("Evicting page " + evictedPage + " (NOT DIRTY)");
        }

        // Reset the page table and dirty bit
        pageTable[evictedPage] = -1;
        dirtyBits[evictedPage] = false;

        return frame;
    }

    private void loadPageIntoMemory(int pageNumber, int frame) throws MemoryException {
        System.out.println("Bringing page " + pageNumber + " into frame " + frame);
        byte[] pageData = disk.readPage(pageNumber);
        byteCount += pageSize;
        pageTable[pageNumber] = frame;
        dirtyBits[pageNumber] = false;

        int frameStartAddress = frame * pageSize;
        for (int offset = 0; offset < pageSize; offset++) {
            int physicalAddress = frameStartAddress + offset;
            memory.writeByte(physicalAddress, pageData[offset]);
        }
    }

    private int calculateAddress(int frame, int virtualAddress) {
        int offset = virtualAddress % pageSize;
        return (frame * pageSize) + offset;
    }

    public void writeBackAllPagesToDisk() throws MemoryException {
        for (int pageNumber = 0; pageNumber < numOfPages; pageNumber++) {
            int frameNumber = pageTable[pageNumber];
            if (frameNumber != -1 && dirtyBits[pageNumber]) {
                byte[] pageData = new byte[pageSize];
                int frameStartAddress = frameNumber * pageSize;
                for (int offset = 0; offset < pageSize; offset++) {
                    int physicalAddress = frameStartAddress + offset;
                    pageData[offset] = memory.readByte(physicalAddress);
                }
                disk.writePage(pageNumber, pageData);
                byteCount += pageSize;
            }
            // Reset the dirty bit
            dirtyBits[pageNumber] = false;
        }
    }

    public void printMemoryContent() throws MemoryException {
        for (int i = 0; i < memory.size(); i++) {
            System.out.println(bToolbox.getBitString(i, bitCount - 1) + ": " + memory.readByte(i));
        }
    }

    public void printDiskContent() throws MemoryException {
        for (int i = 0; i < numOfPages; i++) {
            byte[] page = disk.readPage(i);
            System.out.print("PAGE " + i + ": ");
            for (int j = 0; j < page.length; j++) {
                System.out.print(page[j]);
                if (j < page.length - 1) {
                    System.out.print(",");
                }
            }
            System.out.println();
        }
    }

    public int getPageFaultCount() {
        return pageFault;
    }

    public int getTransferedByteCount() {
        return byteCount;
    }
}
