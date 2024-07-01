package service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetaInfo {

        // 数据区起始索引
        private long dataStart;
        // 数据区长度
        private long dataLen;
        // 稀疏索引区起始索引
        private long indexStart;
        // 稀疏索引区长度
        private long indexLen;

        // 构造函数
        public MetaInfo() {
            this.dataStart = 0;
            this.dataLen = 0;
            this.indexStart = 0;
            this.indexLen = 0;
        }
}
