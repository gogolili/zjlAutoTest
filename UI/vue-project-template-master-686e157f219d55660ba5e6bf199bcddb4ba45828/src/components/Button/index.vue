<template>
    <button @click="handleClickLink" :class="classes">
        <slot>按钮</slot>
    </button>
</template>
<script>
import { prefix, oneOf } from '@/utils/components/common'

const prefixCls = prefix + 'button'
export default {
    name: prefixCls,
    props: {
        type: {
            validator (value) {
                return oneOf(value, ['base', 'white'])
            },
            default: 'base'
        },
        size: {
            validator (value) {
                return oneOf(value, ['large', 'base', 'huge', 'small'])
            },
            default: 'base'
        },
        icon: {
            type: String,
            default: ''
        }
    },
    data () {
        return {
            prefixCls
        }
    },
    computed: {
        classes () {
            return [
                `${prefixCls}`,
                `${prefixCls}-size-${this.size}`,
                `${prefixCls}-type-${this.type}`,
                {
                    [`${prefixCls}-${this.icon}`]: this.icon
                }
            ]
        }
    },
    methods: {
        handleClickLink (event) {
            this.$emit('on-click', event)
        }
    }
}
</script>
<style lang="scss">
    .b-button {
        &-size {
            &-base {
                @extend %title-01;
                width: 100%;
                padding: 32px;
                border-radius: $border-radius-base;
            }
        }

        &-type {
            &-base {
                color: $white;
                background-color: $Primary;
                border-color: $Primary;
                display: inherit;

                &:active {
                    background-color: $LightPrimary;
                    border-color: $LightPrimary;
                }

                border-width: 0;
            }
        }
    }
</style>
