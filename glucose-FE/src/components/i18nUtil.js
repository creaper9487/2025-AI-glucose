import { createI18n } from 'vue-i18n';
const messages = {
    'en-US': {
        hello: 'hello',
        world: 'world',
    },
    'zh-CN': {
        hello: '你好',
        world: '世界',
    },
};

const instance = createI18n({
    locale: 'en-US',
    messages,
});

export default instance;

export const i18n = instance.global;